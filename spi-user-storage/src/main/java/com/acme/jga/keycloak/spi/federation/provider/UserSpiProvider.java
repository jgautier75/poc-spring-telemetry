package com.acme.jga.keycloak.spi.federation.provider;

import com.acme.jga.crypto.CryptoEngine;
import com.acme.jga.keycloak.spi.federation.adapters.UserAdapter;
import com.acme.jga.keycloak.spi.federation.constants.FederationConstants;
import com.acme.jga.keycloak.spi.federation.dtos.UserInfosDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.*;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

public class UserSpiProvider implements UserLookupProvider, UserStorageProvider, CredentialInputValidator {
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_BASIC = "Basic ";
    private static final Logger LOGGER = Logger.getLogger(UserSpiProvider.class);
    private ObjectMapper objectMapper;
    private final ComponentModel storageProviderModel;
    private final KeycloakSession keycloakSession;
    private CryptoEngine cryptoEngine;

    public UserSpiProvider(ComponentModel storageProviderModel, KeycloakSession keycloakSession) {
        this.storageProviderModel = storageProviderModel;
        this.keycloakSession = keycloakSession;
        initialize();
    }

    /**
     * Initialize HTTP client and cryto engine.
     */
    public void initialize() {
        LOGGER.infof("Initialize SPI: [%s]", FederationConstants.ENDPOINT + "=[" + getEnvVariable(FederationConstants.ENDPOINT));
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        cryptoEngine = new CryptoEngine();
    }

    @Override
    public UserModel getUserById(RealmModel realmModel, String userId) {
        return getUser(realmModel, FederationConstants.SearchFilterField.UID, userId);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realmModel, String login) {
        return getUser(realmModel, FederationConstants.SearchFilterField.LOGIN, login);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realmModel, String email) {
        return getUser(realmModel, FederationConstants.SearchFilterField.EMAIL, email);
    }

    /**
     * Get user.<br/>
     * <p>
     * Search user in Keycloak storage, if user does not exist, fetch from poc-st using REST API.
     *
     * @param realmModel        Realm
     * @param searchFilterField Search filter field (login,email,uid)
     * @param value             Search value
     * @return User model
     */
    private UserModel getUser(RealmModel realmModel, FederationConstants.SearchFilterField searchFilterField, String value) {
        UserModel userLocal = keycloakSession.users().getUserById(realmModel, value);
        Optional<UserInfosDto> userInfosDto = Optional.empty();
        if (userLocal == null) {
            userInfosDto = fetchUser(searchFilterField, value);
            if (userInfosDto.isPresent()) {
                userLocal = createUserModel(realmModel, userInfosDto.get());
                initCredentials(realmModel, userInfosDto.get(), userLocal);
            }
        }
        if (userLocal != null) {
            return new UserAdapter(this.keycloakSession, realmModel, storageProviderModel, userInfosDto.get(), userLocal, this.storageProviderModel.getId());
        } else {
            return null;
        }
    }

    /**
     * Get environment variable.
     *
     * @param varName Variable name
     * @return Variable value
     */
    private String getEnvVariable(String varName) {
        return System.getenv(varName);
    }

    /**
     * Create Keycloak user model.
     *
     * @param realmModel Realm
     * @param infosDto   User infos
     * @return User model
     */
    private UserModel createUserModel(RealmModel realmModel, UserInfosDto infosDto) {
        UserModel userLocal = keycloakSession.users().addUser(realmModel, infosDto.getUid(), infosDto.getLogin(), true, false);
        userLocal.setCreatedTimestamp(System.currentTimeMillis());
        userLocal.setEmail(infosDto.getEmail());
        userLocal.setEmailVerified(true);
        userLocal.setEnabled(true);
        userLocal.setFirstName(infosDto.getFirstName());
        userLocal.setLastName(infosDto.getLastName());
        userLocal.setFederationLink(storageProviderModel.getId());
        userLocal.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
        return userLocal;
    }

    /**
     * Fetch user from poc-st.
     *
     * @param searchField Search field (login, email, uid)
     * @param searchValue Serach value
     * @return User infos
     */
    private Optional<UserInfosDto> fetchUser(FederationConstants.SearchFilterField searchField, String searchValue) {
        try (HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(5)).build();) {
            UserInfosDto userInfosDto;
            HttpRequest byUserIdReq = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(buildSearchFilterURI(searchField, searchValue)))
                    .header(AUTH_HEADER, buildBasicAuthHeader())
                    .build();
            HttpResponse<String> response = httpClient.send(byUserIdReq, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                userInfosDto = this.objectMapper.readValue(responseBody, UserInfosDto.class);
            } else if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                userInfosDto = null;
            } else {
                throw new RuntimeException(response.statusCode() + ":" + responseBody);
            }
            return Optional.ofNullable(userInfosDto);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build search filter URI.
     *
     * @param searchField Search field (login, email, id)
     * @param searchValue Search value
     * @return Poc-spring-telemetry find user uri
     */
    private String buildSearchFilterURI(FederationConstants.SearchFilterField searchField, String searchValue) {
        return getEnvVariable(FederationConstants.ENDPOINT) + "?field=" + searchField.name().toLowerCase() + "&value=" + searchValue;
    }

    /**
     * Build basic authentication header.
     *
     * @return Authentication header
     */
    private String buildBasicAuthHeader() {
        String valueToEncode = getEnvVariable(FederationConstants.USER) + ":" + getEnvVariable(FederationConstants.PASSWORD);
        return AUTH_BASIC + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    @Override
    public void close() {
    }

    /**
     * Initialize default credentials for user (if any).
     *
     * @param realm        Realm
     * @param userInfosDto User infos
     * @param userModel    User Model
     */
    private void initCredentials(RealmModel realm, UserInfosDto userInfosDto, UserModel userModel) {
        if (userInfosDto.getEncryptedPassword() != null) {
            String decodedPassword = cryptoEngine.decode(userInfosDto.getEncryptedPassword());
            PasswordPolicy passwordPolicy = realm.getPasswordPolicy();
            Optional<PasswordHashProvider> optHashProvider = lookupPasswordHashProvider(passwordPolicy.getHashAlgorithm());
            Optional<PasswordCredentialProvider> optPassProvider = lookupPasswordCredentialProvider();
            if (optHashProvider.isPresent() && optPassProvider.isPresent()) {
                LOGGER.debugf("Initialize credentials for user [%s,%s,%s]", userModel.getId(), userModel.getUsername(), userModel.getEmail());
                PasswordCredentialModel hashedPassModel = optHashProvider.get().encodedCredential(decodedPassword, passwordPolicy.getHashIterations());
                optPassProvider.get().createCredential(realm, userModel, hashedPassModel);
            }
        }
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        Optional<CredentialModel> storedCredentials = user.credentialManager().getStoredCredentialsStream().filter(sc -> sc.getType().equalsIgnoreCase(PasswordCredentialModel.TYPE)).findFirst();
        String hashAlgo = realm.getPasswordPolicy().getHashAlgorithm();
        Optional<PasswordHashProvider> hashProvider = lookupPasswordHashProvider(hashAlgo);
        boolean valid = false;
        if (storedCredentials.isPresent() && hashProvider.isPresent()) {
            CredentialModel credentialModel = storedCredentials.get();
            PasswordCredentialModel passwordCredentialModel = PasswordCredentialModel.createFromCredentialModel(credentialModel);
            valid = hashProvider.get().verify(credentialInput.getChallengeResponse(), passwordCredentialModel);
        }
        return valid;
    }

    /**
     * Lookup password hash provider.
     *
     * @param hashAlgo Hash algorithm
     * @return PasswordHashProvider
     */
    private Optional<PasswordHashProvider> lookupPasswordHashProvider(String hashAlgo) {
        PasswordHashProvider hashProvider;
        if (hashAlgo != null) {
            hashProvider = keycloakSession.getProvider(PasswordHashProvider.class, hashAlgo);
        } else {
            hashProvider = keycloakSession.getProvider(PasswordHashProvider.class);
        }
        return Optional.ofNullable(hashProvider);
    }

    /**
     * Lookup password credential provider.
     *
     * @return PasswordCredentialProvider
     */
    private Optional<PasswordCredentialProvider> lookupPasswordCredentialProvider() {
        Set<CredentialProvider> allProviders = this.keycloakSession.getAllProviders(CredentialProvider.class);
        Optional<CredentialProvider> optProvider = allProviders.stream().filter(p -> PasswordCredentialModel.TYPE.equalsIgnoreCase(p.getType())).findFirst();
        if (optProvider.isPresent()) {
            PasswordCredentialProvider passwordCredentialProvider = (PasswordCredentialProvider) optProvider.get();
            return Optional.of(passwordCredentialProvider);
        } else {
            return Optional.empty();
        }
    }

}
