package com.acme.jga.rest.controllers;

import com.acme.jga.crypto.CryptoEngine;
import com.acme.jga.rest.config.OpenTelemetryTestConfig;
import com.acme.jga.rest.config.SecurityProperties;
import com.acme.jga.rest.config.VaultSecrets;
import com.acme.jga.utils.test.TestUtils;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureDataSourceInitialization;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
@AutoConfigureDataSourceInitialization
@Testcontainers
@Transactional
@ContextConfiguration(initializers = AppLoadingTest.DataSourceInitializer.class)
@Import(value = {OpenTelemetryTestConfig.class})
class AppLoadingTest {
    private static final String OIDC_BASE_REALM_URI = "/realms/myrealm";
    private static final BasicCredentialsProvider basicAuthProvider = new BasicCredentialsProvider();

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private SecurityProperties securityProperties;

    @MockitoBean
    private VaultSecrets vaultSecrets;

    @MockitoBean
    private CryptoEngine cryptoEngine;

    @RegisterExtension
    private static final WireMockExtension wireMockServer = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.test.database.replace=none",
                    "spring.datasource.url=" + database.getJdbcUrl(),
                    "spring.datasource.username=" + database.getUsername(),
                    "spring.datasource.password=" + database.getPassword());
        }
    }

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(TestUtils.POSTGRESQL_VERSION)
            .waitingFor(Wait.defaultWaitStrategy());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", wireMockServer::baseUrl);
        String issuerUri = wireMockServer.baseUrl() + OIDC_BASE_REALM_URI;
        registry.add("spring.security.oauth2.resourceserver.jwt.issuerUri", () -> issuerUri);
        String jwksUri = wireMockServer.baseUrl() + OIDC_BASE_REALM_URI + "/protocol/openid-connect/certs";
        registry.add("spring.security.oauth2.resourceserver.jwt.jwks_uri", () -> jwksUri);
    }

    @BeforeAll
    public static void initKeycloakAndVault() {
        try {
            String oidcConfig = readResource("keycloak_oidc_config.json");
            wireMockServer.stubFor(get(OIDC_BASE_REALM_URI + "/.well-known/openid-configuration")
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(oidcConfig)));
            String certs = readResource("keycloak_certs.json");
            wireMockServer.stubFor(get(OIDC_BASE_REALM_URI + "/protocol/openid-connect/certs")
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(certs)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream issuerStream = AppLoadingTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            byte[] issuerBytes = issuerStream.readAllBytes();
            int wireMockPort = wireMockServer.getPort();
            return new String(issuerBytes).replaceAll("DYNAMIC_PORT", Integer.toString(wireMockPort));
        }
    }

    @BeforeEach
    void initDb() throws Exception {
        try (Connection conn = DriverManager.getConnection(database.getJdbcUrl(), database.getUsername(), database.getPassword())) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "postgresql/changelogs.xml");
            updateCommand.execute();
        }
    }

    @Test
    void actuator() throws Exception {
        await().pollInterval(Duration.ofSeconds(2L)).atMost(Duration.ofSeconds(6L)).until(actuatorHttpStatus200());
    }

    /**
     * Fetch actuator url.
     *
     * @return Http status code
     */
    private Callable<Boolean> actuatorHttpStatus200() throws UnknownHostException {
        final AtomicInteger httpStatus = new AtomicInteger();
        String hostName = InetAddress.getLocalHost().getHostName();
        String actuatorUrl = "http://" + hostName + ":" + randomServerPort + "/poc-st/actuator";
        HttpHost targetHost = new HttpHost("http", hostName, randomServerPort);
        AuthScope authScope = new AuthScope(targetHost);
        basicAuthProvider.setCredentials(authScope, new UsernamePasswordCredentials(securityProperties.getUserName(), securityProperties.getPass().toCharArray()));

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(basicAuthProvider).disableAutomaticRetries().build()) {
            HttpGet httpGet = new HttpGet(actuatorUrl);
            httpStatus.set(httpClient.execute(httpGet, HttpResponse::getCode));
            log.info("Http response status: {}", httpStatus);
        } catch (Exception e) {
            log.error("Unable to query actuator", e);
        }
        return () -> httpStatus.get() == HttpStatus.OK.value();
    }

}
