package com.acme.jga.keycloak.spi.federation.provider;

import io.github.jopenlibs.vault.SslConfig;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.api.sys.mounts.MountPayload;
import io.github.jopenlibs.vault.api.sys.mounts.MountType;
import io.github.jopenlibs.vault.response.LogicalResponse;
import io.github.jopenlibs.vault.response.MountResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OpenBaoTest {

    @Test
    public void test() throws VaultException {
        String token = "dev-root-token";
        int exposedPort = 8200;

        log.info("Create network");
        Network network = OpenBaoFactory.createNetwork("net-openbao");

        log.info("Create container");
        GenericContainer openBaoContainer = OpenBaoFactory.createOpenBaoContainer(network, "2.2", exposedPort, token);

        log.info("Start container");
        openBaoContainer.start();
        SslConfig sslConfig = new SslConfig();
        sslConfig.build();

        Integer mappedPort = openBaoContainer.getMappedPort(exposedPort);
        VaultConfig vaultConfig = new VaultConfig().token(token).address("http://localhost:" + mappedPort).sslConfig(sslConfig);
        Vault vault = Vault.create(vaultConfig);

        String path = "dev-secrets";
        String cipherKey = "1c9e1cfbe63844b1a0772aea4cba5gg6";

        // Create key/value engine v2
        final MountPayload payload = new MountPayload();
        MountResponse mountResponse = vault.sys().mounts().enable(path, MountType.KEY_VALUE_V2, payload);
        log.info("Response status [" + mountResponse.getRestResponse().getStatus() + "] response body [" + new String(mountResponse.getRestResponse().getBody()) + "]");


        // Put secret
        Map<String, Object> values = new HashMap<>();
        values.put("cipherKey", cipherKey);
        LogicalResponse response = vault.logical().write(path + "/data/creds", values);

        // Read secret
        LogicalResponse logicalResponse = vault.logical().read(path + "/data/creds");
        int responseStatus = logicalResponse.getRestResponse().getStatus();
        String responseBody = new String(logicalResponse.getRestResponse().getBody());
        log.info("Response status [" + responseStatus + "] response body [" + responseBody + "]");

        Assertions.assertNotNull(logicalResponse, "Response is not null");
        Map<String, String> responseData = logicalResponse.getData();
        Assertions.assertEquals(200, responseStatus, "Response status 200");
        Assertions.assertEquals(cipherKey, logicalResponse.getData().get("cipherKey"), "Cipher key match");
    }

}
