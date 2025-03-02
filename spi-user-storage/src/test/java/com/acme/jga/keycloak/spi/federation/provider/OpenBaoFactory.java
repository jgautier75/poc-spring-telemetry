package com.acme.jga.keycloak.spi.federation.provider;

import lombok.NoArgsConstructor;
import org.junit.runner.Description;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@NoArgsConstructor
public class OpenBaoFactory {

    public static Network createNetwork(String networkId) {
        Network defaultDaprNetwork = new Network() {
            @Override
            public org.junit.runners.model.Statement apply(org.junit.runners.model.Statement base, Description description) {
                return null;
            }

            @Override
            public String getId() {
                return networkId;
            }

            @Override
            public void close() {
            }
        };
        List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter(networkId).exec();
        if (networks.isEmpty()) {
            Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName(networkId))
                    .build().getId();
            return defaultDaprNetwork;
        } else {
            return defaultDaprNetwork;
        }
    }

    public static GenericContainer createOpenBaoContainer(Network network, String version, int exposedPort, String rootToken) {
        return new GenericContainer<>("quay.io/openbao/openbao:" + version)
                .withExposedPorts(exposedPort)
                .withNetworkAliases("openbao")
                .withNetwork(network)
                .withEnv("OPENBAO_SKIP_VERIFY", "true")
                .withEnv("BAO_DEV_ROOT_TOKEN_ID", rootToken)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS));
    }
}
