package com.acme.jga.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.net.UnknownHostException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;

@Slf4j
class KafkaSimpleConsumerTest {
    public static final String CONFLUENT_PLATFORM_VERSION = "7.7.1";
    private static final String KAFKA_NETWORK = "kafka-network";
    private static final int SCHEMA_REGISTRY_PORT = 8085;
    private static final int KAFKA_LISTENER_PORT = 19092;

    @Test
    public void test() throws UnknownHostException {

        // Start kafka and schema registry containers
        Network network = getNetwork();
        ConfluentKafkaContainer confluentKafkaContainer = new ConfluentKafkaContainer("confluentinc/cp-kafka:" + CONFLUENT_PLATFORM_VERSION)
                .withListener("kafka:" + KAFKA_LISTENER_PORT)
                .withNetwork(network)
                .withReuse(true);
        GenericContainer<?> schemaRegistry = new GenericContainer<>("confluentinc/cp-schema-registry:" + CONFLUENT_PLATFORM_VERSION)
                .withExposedPorts(SCHEMA_REGISTRY_PORT)
                .dependsOn(confluentKafkaContainer)
                .withNetworkAliases("schemaregistry")
                .withNetwork(network)
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:" + KAFKA_LISTENER_PORT)
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:" + SCHEMA_REGISTRY_PORT)
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schemaregistry")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
                .waitingFor(Wait.forHttp("/subjects"))
                .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS));
        schemaRegistry.start();
        String baseUrl = String.format("http://%s:%d", schemaRegistry.getHost(), schemaRegistry.getMappedPort(SCHEMA_REGISTRY_PORT));
        String schemasUrl = baseUrl + "/subjects";
        await().pollInterval(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(45)).until(subjects200(schemasUrl));
    }

    private Callable<Boolean> subjects200(String targetUrl) throws UnknownHostException {
        final AtomicInteger httpStatus = new AtomicInteger();
        int timeout = 2000;
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout, TimeUnit.MILLISECONDS).setResponseTimeout(timeout, TimeUnit.MILLISECONDS).build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).disableAutomaticRetries().build()) {
            log.info("Trying " + targetUrl);
            HttpGet httpGet = new HttpGet(targetUrl);
            httpStatus.set(httpClient.execute(httpGet, HttpResponse::getCode));
            log.info("Http response status: {}", httpStatus);
        } catch (Exception e) {
            log.error("Unable to query schema registry subjects uri", e);
        }
        return () -> httpStatus.get() == HttpStatus.SC_OK;
    }

    private Network getNetwork() {
        Network defaultDaprNetwork = new Network() {
            @Override
            public org.junit.runners.model.Statement apply(org.junit.runners.model.Statement base, Description description) {
                return null;
            }
            @Override
            public String getId() {
                return KAFKA_NETWORK;
            }
            @Override
            public void close() {
            }
        };
        List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter(KAFKA_NETWORK).exec();
        if (networks.isEmpty()) {
            Network.builder()
                    .createNetworkCmdModifier(cmd -> cmd.withName(KAFKA_NETWORK))
                    .build().getId();
            return defaultDaprNetwork;
        } else {
            return defaultDaprNetwork;
        }
    }

}