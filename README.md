# Users Management

[Multi-tenant](https://en.wikipedia.org/wiki/Multitenancy) designed spring-boot application for users management.

A multi-tenants software architecture aims at serving multiple organizations.

Code is organized in a hexagonal architecture style where:

- adapter-rest    : REST exposition layer, controller advice, log http filter, request correlation filter
- adapter-kafka   : Kafka consumer (audit events)
- ports           : a.k.a "primary port" or "input port" services dedicated to exposition layer (REST or Kafka consumer)
- domain-model    : Domain entities model
- domain-services : Domain services (business rules) , agnostic to exposition layers and underlying storage layer
- infrastructure  : Storage layer

Other modules:
- cryto           : AES cipher/decipher engine
- dao-lib         : DAO utilities (AbstractJdbcDaoSupport, SQLExtractor, DaoConstants)
- db-migration    : Liquibase database migration changeLogs
- docker          : Docker-compose files (grafana, jaeger, openbao, opentelemetry, prometheus)
- kube            : Helm chart for database init and rest apis deployment
- logging         : Transverse logging service
- opentelemetry   : OpenTelemetry wrapper
- search-filter   : ANTLR4 for search filtering
- shared-utils    : Request correlation id (ThreadLocal), Streams util
- spi-kafka       : Keycloak Spi, a kafka consumer consumming audit events in order to update user's basic infos
- spi-user-storage: Keycloak Spi, a keycloak user provider coupled with users management's REST api
- spring-native   : Reflect configuration file required for application compilation in native format
- validation      : Abstract validation engine (payload validation)

## Technical stack

Standard REST application relying on:

- Java 23 ([Graalvm](https://www.graalvm.org/downloads/) for native images )
- Sprint boot (3.5.x)
- PostgreSQL (17.x) for persistence
- Liquibase for rdbms schema versions management
- Spring JDBC for persistence (No f****** ORM)
- Spring integration for PublishSubscribe channel
- Keycloak
- OpenBao
- OpenTelemetry
- Kafka stack (Kafka + Zookeeper + Schema registry + AKHQ)
- Junit/Mockito/Testcontainers for testing

## Docker

Start using the following order:

* [Docker - Base] to start base containers
* Either [Docker - Jaeger] or [Docker - Grafana Loki - Grafana Tempo]
* [OpenBao & Kafka setup] to initialize OpenBao secrets and to create kafka topics

## Docker - Base

Use docker/setup_base.sh script to start the following "base" containers/services:

| Service             | Version | Port    | Description                               |
|---------------------|---------|---------|-------------------------------------------|
| postgreSQL          | 17.5    | 5432    | Spring app storage                        |
| keycloak            | 26.2.5  | 7080    | Keycloak dev instance                     |
| keycloak-postgreSQL | 17.5    | 5433    | Keycloak app storage                      |
| openbao             | 2.2.2   | 8200    | OpenBao port                              |
| akhq                | 0.25.1  | 8086    | GUI for kafka (topics, consumers, ...)    | 
| zookeeper           | 7.9.1   | 2181    | Centralized service for kafka management  |
| kafka               | 7.9.1   | 9092    | Kafka broker                              |
| schema-registry     | 7.9.1   | 8085    | Schema registry (protobuf schemas storage |

## Docker - Jaeger

Use docker/docker-services-jaeger.yml to start the "base" containers described above +

```shell
docker-compose -f docker-services-jaeger.yml up -d
```

| Service                         | Version | Port              |
|---------------------------------|---------|-------------------|
| jaeger-all-in-one               | 1.69.0  | 16686             |
| prometheus                      | v3.4.1  | 9090              |
| grafana                         | 12.0.1  | 3000              |
| opentelemetry-collector-contrib | 0.127.0 | 4317, 4318, 55679 |

## Docker - Grafana Loki - Grafana Tempo

Source: https://github.com/grafana/docker-otel-lgtm/tree/main

Run docker/run_lgtm.sh

After a while, the following logs are displayed:

````
Open ports:
- 4317: OpenTelemetry GRPC endpoint
- 4318: OpenTelemetry HTTP endpoint
- 3000: Grafana. User: admin, password: admin
````

## OpenBao & Kafka setup

To initialize OpenBao and Kafka topics creation run the following ansible command.

For testing purposes, vault password is set to "test"

```sh
ansible-playbook -i inventory/hosts.ini docker-setup.yml --connection=local --ask-vault-pass
```

## Database setup


#### Manually

Database schema management relies on liquibase, to setup:

1. Connect to postgreSQL container and execute:

- Creates database: create_database.sql
- Creates accounts: accounts_setup.sql
    - poc_st_dba: database account with DDL authorizations (Data Definition Language)
    - poc_st_spp: database account with only DML authorizations (Data Modeling Language). This account is used by spring boot application since applicative accounts must not have the rights to alter database schema

2. Package project

```java
mvn clean package install -DskipTests
```

3. Move to db-migration/target folder

4. Perform Liquibase update manually:

```java
java -jar db-migration.jar --classpath=db-migration.jar --driver=org.postgresql.Driver --url="jdbc:postgresql://localhost:5432/poc-st" --changeLogFile="postgresql/changelogs.xml" --username=poc_st_dba --password=poc_st_dba --logLevel=info --contexts="all,grants" update
```

#### Docker image

Building the database init docker image:

```sh
docker build . -t db-migration:1.2.0 --build-arg="JAR_FILE=target/db-migration.jar" --build-arg="INITSH=scripts/init.sh" --build-arg="ACTSQLFILE=sql/accounts_setup.sql" --build-arg="DBSQLFILE=sql/create_database.sql" --build-arg="LIQUITEMP=liquibase/liquibase_template.properties" --build-arg="GRANTSTEMP=sql/grants_template.sql" --build-arg="GRANTSDBA=sql/grants_dba_template.sql"
```

```sh
docker run -it --env P_PGHOST=192.168.1.15 --env P_PGPORT=5432 --env P_PGUSER=postgres --env P_PGPASS=posgres --env P_DBNAME=orm-test --env P_DBAUSER=orm_dba --env P_DBAPASS=dba_pass --env P_APPUSER=orm_app --env P_APPPASS=pass_app ec29fd57abd9
```

## OpenBao

OpenBao is a fork of HashiCorp vault.

By default OpenBao is started id dev mode, "dev-root-token" is used in configuration.

Use Bruno collection to add/list secrets.

```bash
export VAULT_ADDR='http://127.0.0.1:8200'
vault login
vault kv get -mount=dev-secrets creds
vault kv put -mount=dev-secrets creds foo=bar
```

Api call example:

```bash
curl -H "X-Vault-Token: dev-root-token" -X GET http://192.168.1.13:8200/v1/dev-secrets/data/creds
```

Config HTTP status:

```bash
HTTP_STATUS=$(curl -H "X-Vault-Token: dev-root-token" -w "%{http_code}" -o >(cat >&3) 'http://192.168.1.15:8200/v1/dev-secrets/config' ); 
echo "HTTP_STATUS: $HTTP_STATUS"
```

Inserting a secret:

```bash
curl -H "X-Vault-Token: dev-root-token" --request POST --data @vault_data.json http://192.168.1.15:8200/v1/dev-secrets/data/creds
```

Delete secret version:

```bash
curl -v -H "X-Vault-Token: dev-root-token" --request PUT --data @vault_versions_delete.json http://192.168.1.15:8200/v1/dev-secrets/data/creds
```

Example:

```json
{
  "request_id": "cfbe37cc-a076-000c-5911-b23cd8c213a1",
  "lease_id": "",
  "renewable": false,
  "lease_duration": 0,
  "data": {
    "data": {
      "cipherKey": "1c9e1cfbe63844b1a0772aea4cba5gg6"
    },
    "metadata": {
      "created_time": "2025-02-21T13:13:26.902464695Z",
      "custom_metadata": null,
      "deletion_time": "",
      "destroyed": false,
      "version": 1
    }
  },
  "wrap_info": null,
  "warnings": null,
  "auth": null
}
```

If jq is intalled, secret values can be extracted using a command like:

```bash
curl -H "X-Vault-Token: dev-root-token" -X GET http://192.168.1.13:8200/v1/dev-secrets/data/creds | jq '.data.data'
```

## OpenTelemetry

Quote from OpenTelemetry website: "OpenTelemetry is a collection of APIs, SDKs, and tools. Use it to instrument, generate, collect, and export telemetry data (metrics, logs, and traces)"

OpenTelemetry main component is called the collector which collects metrics,logs and traces and uses various exporters like prometheus, grafana loki, grafana tempo, jaeger, ...

Different docker configurations are provided in this project, jaeger or grafana loki (for logs) and tempo (e.g for counter, gauges, ...)

## Entities

- **Tenant**:
    - A tenant aims at serving multiple organizations.
    - Properties:
        - id: internal identifier
        - uid: external identifier (UUID)
        - code: functional code (unique)
        - label: tenant's label
- **Organization**:
    - An organization belongs to a tenant and holds users
    - Properties:
        - id: internal identifier
        - uid: external identifier (UUID)
        - tenant: Reference on tenant
        - label: Organization's label
        - code: functional code (unique)
        - kind: Organization's code (Enumeration: TENANT,BU,COMMUNITY,ENTERPRISE)
        - country: Country code (ISO 3166-1 Alpha2)
        - status: status (Enumeration: DRAFT, ACTIVE, INACTIVE)
- **Sector**:
    - A sector is a logical subdivision of an organization.
    - An organization always have a root sector.
    - A root sector cannot be deleted.
    - Properties:
        - id: internal identifier
        - uid: external identifier (UUID)
        - code: functional code (unique)
        - label: sector's label
        - parentId: Parent sector internal identifier
        - root: boolean, is current sector the root sector
- **User**:
    - A user belongs to an organization and thus to a tenant
    - Properties:
        - id: internal identifier
        - uid: external identifier (UUID)
        - tenant_id: Reference on tenant
        - org_id: Reference on organization
        - login: User login (unique)
        - firstName: First name
        - lastName: Last name
        - middleName: Middle name
        - email: Email address
        - status: (Enumeration: DRAFT, ACTIVE, INACTIVE)
        - notif_email: Email for notifications (non unique, e.g: a diffusion list)
- **Events**:
    - Storage of audit events.
    - An audit event is always recorded when an entity is created (tenant, organization, sector, user), updated or deleted
    - Properties:
        - uid: A unique identifier (uuid)
        - created_at: Creation timestamp (UTC/ISO-8601)
        - last_updated_at: Last update timestamp (UTC/ISO-8601)
        - target: Entity type (Enumeration: Tenant(0), Organization(1), User(2), Sector(3))
        - object_uid: Entity object uid
        - action: Enumeration: CREATE, UPDATE, DELETE
        - status: Event status (Enumeration: PENDING(0), PROCESSED(1), FAILED(2))
        - payload: Audit event in json format (PostgreSQL jsonb)

## OpenAPI

REST endpoints OpenApi specifications: docs/poc_st_openapi.yml

Transform OpenAPI spec to html with redocly:

```bash
npm install @redocly/cli -g
```

Transform to HTML:
```bash
redocly build-docs ../poc_st_openapi.yml
```

Generating java code from openapi specification, first download the latest version of openapi-generator-cli on mvnrepository.com site

Then create a json config file:

```json
{
  "basePackage": "com.tutorial.codegen",
  "configPackage": "com.tutorial.codegen.config",
  "apiPackage": "com.tutorial.codegen.controllers",
  "modelPackage": "com.tutorial.codegen.model",
  "groupId": "com.tutorial",
  "artifactId": "spring-boot-codegenerator"
}

```

Http client generation:

```
java -jar openapi-generator-cli-7.13.0.jar generate -g java -i poc_st_openapi.yml -c openapi-gen-config.json -o spring-boot-codegenerator
```

Server side generation:

```
java -jar openapi-generator-cli-7.13.0.jar generate -g client -i poc_st_openapi.yml -c openapi-gen-config.json -o spring-boot-codegenerator
```

## Audit events

Everytime an entity (tenant, organization, user, sector) is created, updated or deleted, an audit event is persisted in rdbms.

**Why persisting audit events in rdbms and not sending event directly to kafka ?**

When talking about audit events, we must ensure events and related data in rdbms are **consistent**.

Indeed, we want to avoid the following two use cases:

- A rollback is performed in rdbms but the event is still pushed to kafka and thus the audit event does not reflect the underlying data.
- For some reasons, the kafka brokers are not reacheable (network failure for example) even after many retries.

Thus, to ensure consistency between data stored in rdbms and audit event, these are stored in rdbms in the same transaction as the data. Obviously, we're here relying on ACID features of postgreSQL relational database.
In other words, if a transaction rollback occurs, both data and audit events are rollbacked.

![](docs/images/AuditEvents.drawio.png)

Schema above describes this behaviour, the audit event is created in the same transaction than data.
Once audit events and data have been persisted, a "wakeup" message is sent to a spring-integration PublishSubscribe channel

```java
eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
```

Listener "eventAuditChannel" responsibilities are:

- Retrieve "pending" events from rdbms
- Push events to kafka
- Mark events are processed

![](docs/images/AuditEventListener.drawio.png)

Message in kafka topic:

![](docs/images/akhq_kafka_msg.png)

## Protobuf

Messages formats sent to kafka are likely to change across time (new fields, refactoring, ...). Furthermore, you cannot expect all consumers to migrate to the new version at the same time. As a consequence, messages versioning must be handled.

Google [Protobuf](https://protobuf.dev) provides a convenient way to manage versioning and also offers better performance than standard json or other binary formats.

Protobuf relies on message definitions in [.protoc](https://protobuf.dev/programming-guides/proto3/) format (see protobuf-model/src/main/protobuf/event.proto).

To generate java pojos from .proto file, execute the following command:

```sh
protoc -I protobuf-model/src/main/protobuf --java_out=protobuf-model/src/main/java protobuf-model/src/main/protobuf/event.proto
```

Audit events are sent to kafka using protobuf format (see EventBusHandler class).

In case multiple versions of messages are available (e.g: a v1 and a v2), how to we know at consumer side which version to use ?

This is where schema-registry comes into play.

Basically, schema-registry stores in a versioned way messages definitions.

To list schemas-registry stored schemas, just use a simple [REST api](https://docs.confluent.io/platform/current/schema-registry/develop/api.html):

```sh
curl -L http://localhost:8085/schemas
```

Response:

```json
[
	{
		"subject": "audit_events-value",
		"version": 1,
		"id": 1,
		"schemaType": "PROTOBUF",
		"schema": "syntax = \"proto3\";\npackage com.acme.users.mgt.events.protobuf;\n\nmessage AuditEventMessage {\n  string createdAt = 1;\n  string lastUpdatedAt = 2;\n  string uid = 3;\n  int32 target = 4;\n  .com.acme.users.mgt.events.protobuf.AuditAuthor author = 5;\n  .com.acme.users.mgt.events.protobuf.AuditScope scope = 6;\n  string objectUid = 7;\n  string action = 8;\n  int32 status = 9;\n  repeated .com.acme.users.mgt.events.protobuf.AuditChange changes = 10;\n}\nmessage AuditAuthor {\n  string uid = 1;\n  string name = 2;\n}\nmessage AuditScope {\n  string tenantUid = 1;\n  string tenantName = 2;\n  string organizationUid = 3;\n  string organizationName = 4;\n}\nmessage AuditChange {\n  string object = 1;\n  string from = 2;\n  string to = 3;\n}\n"
	},
	{
		"subject": "audit_events-value",
		"version": 2,
		"id": 2,
		"schemaType": "PROTOBUF",
		"schema": "syntax = \"proto3\";\npackage com.acme.users.mgt.events.protobuf;\n\nmessage AuditEventMessage {\n  string createdAt = 1;\n  string lastUpdatedAt = 2;\n  string uid = 3;\n  int32 target = 4;\n  .com.acme.users.mgt.events.protobuf.AuditAuthor author = 5;\n  .com.acme.users.mgt.events.protobuf.AuditScope scope = 6;\n  string objectUid = 7;\n  string action = 8;\n  int32 status = 9;\n  repeated .com.acme.users.mgt.events.protobuf.AuditChange changes = 10;\n}\nmessage AuditAuthor {\n  string uid = 1;\n  string name = 2;\n}\nmessage AuditScope {\n  string tenantUid = 1;\n  string tenantName = 2;\n  string organizationUid = 3;\n  string organizationName = 4;\n}\nmessage AuditChange {\n  string object = 1;\n  string from = 2;\n  string to = 3;\n  string operation = 4;\n}\n"
	}
]
```

To validate a schema:
```sh
mvn schema-registry:validate
```

To push a schema into registry, use the following maven command:
```sh
mvn schema-registry:register
```

Obviously the schema above is the content of the .proto file define earlier in this document.

If schema registry is configured in AKHQ, messages can be decoded into a human-readable format:

![](docs/images/akhq_protobuf.png)

In this spring-boot prototype, spring send automatically schemas to registry.

Consumer configuration points to schema registry:

```java
@Bean
Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, clientId);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
    return props;
}

@Bean
KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(3);
    factory.getContainerProperties().setPollTimeout(3000);
    return factory;
}
```

Listener itself:
```java
@Service
@RequiredArgsConstructor
public class KafkaSimpleConsumer {
    private final ILogService logService;

    @KafkaListener(topics = "${app.kafka.producer.topicNameAuditEvents}", groupId = "${app.kafka.consumer.auditEventsGroupId}")
    public void consume(ConsumerRecord<String, DynamicMessage> messageRecord) throws InvalidProtocolBufferException {
        Event.AuditEventMessage auditEventMessage = Event.AuditEventMessage.newBuilder().build().getParserForType().parseFrom(messageRecord.value().toByteArray());
        loggingFacade.infoS(this.getClass().getName(), "Received message with key [%s] and content : [%s]", new Object[]{messageRecord.key(), auditEventMessage.toString()});
    }

}
```

## Testing REST APIS

An [Bruno](https://www.usebruno.com/) collection (UsersManagement.json) is available in docs directory.

**WARNING** system endpoints like actuator and system endpoints are secured using basic authentication, other endpoints are secured using oauth2

## Debugging HTTP requests

A particular HTTP filter (see: LogHttpFilter class in webapi module) has been designed to be able to debug a single HTTP request.

Indeed, the problem with changing level mode of loggers is that all incoming traffic is debugged.

To avoid this, since it's a classic REST API, one HTTP request => One thread.

Thus, a ThreadLocal (LogHttpUtils.APP_LOG_CTX) can be used to position a debug flag depending on the presence or absence of an HTTP header in request.

This custom header is customizable in application.yml (Defaults to X-APP-DEBUG)

Moreover, the unique Spring LogService logs requests depending of the ThreadLocal value set by filter.

Finally, by default all requests are debugged, this behaviour is controlled by "forceDebugMode" parameter in application.yml

## Technical errors:

When a technical error occurs, the controller advice generates a technical report file and a micrometer gauge is incremented (see MicrometerPrometheus class).

Moreover, the gauge value is exported in actuator/prometheus endpoint and thus can be scrapped by a prometheus job (tech_errors is example below).

![](docs/images/micrometer-prometheus-gauge.png)

## Minimizing JDK in Docker image for spring-boot application

In order to minimize java runtime in webapi Docker image, jdeps tool must be used to determine which java jdk modules are used by application.

To achieve this, first compile package maven project.

Once project bas been compiled, run scripts/get-spring-boot-modules.sh:

Parameters:

- 1: Full path to spring-boot fat jar
- 2: Jdk version (23)
- 3: Temp directory for spring-boot app extraction
- 4: Automatic modules: list of automatic modules, typically legacy libraries (multiple values separator is the comma)

```sh
./get-springboot-modules.sh adapter-rest/target/adapter-rest-1.0.0-SNAPSHOT.jar 23 adapter-rest/target/tmp ""
```

Update adapter-rest/Dockerfile accordingly in jlinks section

`RUN jlink --compress=2 --no-header-files --no-man-pages --add-modules java.base,java.desktop,java.instrument,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql.rowset,jdk.compiler,jdk.jfr,jdk.management,jdk.unsupported,jdk.crypto.ec  --output /app/customjre`

Building Docker image:

```sh
docker build . -t adapter-rest:1.0.0 --build-arg="JAR_FILE=target/adapter-rest-1.0.0-SNAPSHOT.jar"
```

## Native image with GraalVM

Prerequisite: Graalvm installed https://www.graalvm.org/downloads/

GCC installed:

```sh
apt-get install build-essential
```

To build a native image run the following command with "native" profile:

```sh
mvn clean package -DskipTests -Pnative
```

On a computer with an i7 and 16 Gb of RAM, building takes around 6 minutes

Command above relies on https://graalvm.github.io/native-build-tools/latest/maven-plugin.html

Plugin configuration example in webapi/pom.xml file

```xml
<profile>
    <id>native</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.graalvm.buildtools</groupId>
                <artifactId>native-maven-plugin</artifactId>
                <version>0.10.4</version>
                <executions>
                    <execution>
                        <id>build-native</id>
                        <goals>
                            <goal>compile-no-fork</goal>
                        </goals>
                        <configuration>
                            <buildArgs>
                                <arg>-H:+UnlockExperimentalVMOptions</arg>
                                <arg>-H:IncludeResources=.*properties$</arg>
                                <arg>-H:ReflectionConfigurationFiles=./spring-native/reflect-config.json</arg>
                                <arg>
                                    --trace-class-initialization=org.apache.commons.logging.LogFactoryService,org.apache.commons.logging.LogFactory
                                </arg>
                            </buildArgs>
                        </configuration>
                        <phase>package</phase>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

**TIPS**

AOT(**A**head **O**f **T**ime) is a static compilation and thus reflection mechanisms cannot be handled the same way than JIT (**Just** **In** **T**ime).

For example, to use reflection like jackson when serializing / deserializing from/to json, description of fields and methods of DTOs (**Data** **T**ransfer **Object**) might be required.

To achieve this, a reflect-config file must be designed to indicate how to serialize / deserialize from a DTO / JSON.

See https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/

In this project, spring-native/reflect-config.json file describes classes like:

- AuditEvent: These objects are serialized in json before being persisted in rdbms
- AuditScope: A nested object of AuditEvent
- ApiError: Standard POJO returned as json when an error occurs in REST controllers

**2024-02-19** Update:

Integrating kafka with protobuf is not an easy thing.

Indeed, the problem with kafka & protobuf in a native image is that compilation succeeds but fails at runtime.

Thus, the reflect-config.json file grew up to more than 500 lines with kafka & protobuf integration.

https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html

**Youtube Spring I/O**:

- https://www.youtube.com/watch?v=8umoZWj6UcU
- https://www.youtube.com/watch?v=HWUy0kTlcj8

## Keycloak

Default realm: myrealm

Default credentials:

* Administrator: admin/admin
* User: myuser/mypass

Activity diagram for authentication with Keycloak using a SPI federation API.

![](docs/images/UserAuth.png)

### User provider SPI (spi-user-storage)

SPI API on Keycloak side is a jar with a class implementing at least:

- org.keycloak.storage.UserStorageProvider;
- org.keycloak.storage.user.UserLookupProvider;

In this jar, declare a file in /src/main/resources/META-INF/services/org.keycloak.storage.UserStorageProviderFactory containing implementation class.

> spi-user-storage jar is a fat jar, in other words it includes all dependencies


### Kafka consumer SPI (spi-kafka)

This module deploys a kafka consumer listening to audit_events topic in order to update users firstName, lastName & email.

> spi-kafka is also a fat jar. However, to minimize jar's size, dependencies can be copied in /opt/keycloak/lib/lib/main folder (see docker-services.yml file where kafka-clients jar is copied in keycloak container)
> Moreover, if a dependency is copied in keycloak container, don't forget ro mark dependecy with scope provided in spi pom.xml file and to remove/comment target library in maven-shade-plugin configuration

### Docker image (optimized)

For production use, first build an optimized version of keycloak with Dockerfile located in docker/keycloak-image:

```bash
docker build . -t keycloak-with-spi:1.0.0 --build-arg="SPI_FEDERATION_FILE=spi-user-storage.jar" --build-arg="SPI_KAFKA_FILE=spi-kafka.jar"
```

Then to run keycloak with the following variables:


* KC_SPI_TRUSTSTORE_FILE_FILE: truststore file 
* KC_SPI_TRUSTSTORE_FILE_PASSWORD: truststore password 
* KC_PROXY = edge

```bash
/opt/keycloak/bin/kc.sh start --optimized
```

## Minikube:

Enabling ingress (nginx):

```sh
minikube addons enable ingress
```

Verifying nginx ingress is up:

```sh
kubectl -n ingress-nginx get pods
```

Using minikube docker

```sh
minikube docker-env
```

```sh
eval $(minikube -p minikube docker-env)
```

Listing images:

```sh
minikube image ls --format table
```

### Init Database

Please refer to Docker > Database setup to build image.

In kube/poc-st-db-init/values.yaml, ensure parameters are the right ones.

Note: db-migration scripts are idempotent, so if database and/or accounts already exist, they are not re-created.

When image db-migration:x.y.z is ready, run the folling helm command in kube directory:
```sh
helm template pos-st-db-init poc-st-db-init | kubectl apply -f -
```

Command above will deploy a pod in kubernetes, this pod performs liquibase update.

### WebApi

Either save image and load in minikube:

```sh
docker save 4ed63bb1fddb --output poc-st-webapi.tar
minikube image load poc-st-webapi.tar
```

Or build image in minikube:

```sh
minikube image build . -t poc-st-webapi:1.0.0 --build-env="JAR_FILE=target/webapi-1.0.0-SNAPSHOT.jar"
```

Templating with helm and deploying:

```sh
helm template poc-st-webapi poc-st-webapi | kubectl apply -f -
```

Opening service endpoint

```sh
minikube service poc-st-webapi
```

## OpenTelemetry

### JAEGER

Goal of [OpenTelemetry](https://opentelemetry.io/) is used to instrument, generate, collect, and export telemetry data (metrics, logs, and traces) to help you analyze your software’s performance and behavior.

OpenTelemetry collector config: otel-collector-config.yml

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: otel-collector:4317
  otlp/2:
    protocols:
      grpc:
        endpoint: otel-collector:55679

exporters:
  otlp/jaeger: # Jaeger supports OTLP directly. The default port for OTLP/gRPC is 4317
    endpoint: http://jaeger:4317
    tls:
      insecure: true
  logging:
    verbosity: detailed
    sampling_initial: 5
    sampling_thereafter: 200
  prometheus:
    endpoint: "0.0.0.0:8889"

processors:
  batch:

extensions:
  health_check:

service:
  extensions: [health_check]
  pipelines:    
    traces:
      receivers: [otlp,otlp/2]
      processors: [batch]
      exporters: [otlp/jaeger]
    logs:
      receivers: [otlp,otlp/2]
      processors: [batch]
      exporters: [logging]
    metrics:
      receivers: [otlp,otlp/2]
      processors: [batch]
      exporters: [logging,prometheus]
```

No additional dependency required in maven project.

Launch application with the following arguments:

```sh
java -javaagent:/home/jgautier/git-data/opentelemetry-javaagent.jar -Dotel.service.name=poc-st-webapi -Dotel.traces.exporter=jaeger -Dotel.exporter.otlp.protocol=http/protobuf -Dotel.javaagent.debug=true -Dotel.metrics.exporter=otlp -Dotel.logs.exporter=none -jar webapi-1.0.0-SNAPSHOT.jar
```

Update -Dotel.javaagent.debug=true argument to disable opentelementry java agent debug mode.

NB: If you wanna launch application with java agent instrumentation, ensure to comment OpenTelemetryConfig.java configuration class !

OpenTelemetry spans logs:

```sh
otel.javaagent 2024-01-17 22:36:06:457 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'SELECT poc_st.tenants' : 62dd1652c7fe3942a5c33403e84f0f44 00c752df75943a17 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=SELECT, db.sql.table=tenants, db.name=poc_st, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=poc_st_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=select id,uid,code,label from tenants where (uid=?), server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:460 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'SELECT poc_st.organizations' : 62dd1652c7fe3942a5c33403e84f0f44 edb8737ec0cc3856 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=SELECT, db.sql.table=organizations, db.name=poc_st, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=poc_st_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=select id from organizations where code=?, server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:463 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT poc_st.organizations' : 62dd1652c7fe3942a5c33403e84f0f44 ca323db9cd220da5 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=organizations, db.name=poc_st, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=poc_st_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into organizations(tenant_id,uid,code,label,kind,country,status) values (?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:469 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT poc_st.events' : 62dd1652c7fe3942a5c33403e84f0f44 fb12855a3d92f909 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=events, db.name=poc_st, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=poc_st_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into events(uid,created_at,last_updated_at,target,object_uid,action,status,payload) values(?,?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
[otel.javaagent 2024-01-17 22:36:06:470 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT poc_st.sectors' : 62dd1652c7fe3942a5c33403e84f0f44 8021aa0286c870ad CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=sectors, db.name=poc_st, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=poc_st_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into sectors(uid,tenant_id,org_id,label,code,root,parent_id) values(?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
2024-01-17 22:36:06,471 INFO  [http-nio-8080-exec-1] - com.acme.users.mgt.infra.services.impl.sectors.SectorsInfraServicecreateSector: Created sector with uid [8939cdfb-7543-46bb-8626-a03d3cf345f2] on tenant [1] and organization [5]
[otel.javaagent 2024-01-17 22:36:06:472 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'INSERT poc_st.events' : 62dd1652c7fe3942a5c33403e84f0f44 f50362fdb0ee9610 CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=INSERT, db.sql.table=events, db.name=poc_st, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=poc_st_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=insert into events(uid,created_at,last_updated_at,target,object_uid,action,status,payload) values(?,?,?,?,?,?,?,?), server.port=5432}, capacity=128, totalAddedValues=11}
2024-01-17 22:36:06,483 DEBUG [http-nio-8080-exec-1] - com.acme.users.mgt.events.kafka.handlers.EventBusHandler: Handling wakeup message
[otel.javaagent 2024-01-17 22:36:06:485 +0100] [http-nio-8080-exec-1] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'SELECT poc_st.events' : 62dd1652c7fe3942a5c33403e84f0f44 7d3459dbd94ab94c CLIENT [tracer: io.opentelemetry.jdbc:2.0.0-alpha] AttributesMap{data={db.operation=SELECT, db.sql.table=events, db.name=poc_st, thread.name=http-nio-8080-exec-1, thread.id=49, db.user=poc_st_app, db.connection_string=postgresql://localhost:5432, server.address=localhost, db.system=postgresql, db.statement=select uid,created_at,last_updated_at,target,object_uid,action,status,payload from events where (status=?) order by created_at ASC, server.port=5432}, capacity=128, totalAddedValues=11}
```


Organization creation in Jaeger UI:

With instrumentation using java agent

![](docs/images/otel_create_orga.png)

With programmatic spans:

![](docs/images/opentelemetry_org_create.png)

### LOKI & TEMPO

Test counters & logging methods are registered in OpenTelemetryWrapper:

```java
@Component
public class OpenTelemetryWrapper implements InitializingBean {
  @Autowired
  private TracerProvider sdkTracerProvider;
  @Autowired
  private MeterProvider sdkMeterProvider;
  @Autowired
  private LoggerProvider sdkLoggerProvider;
  private LongCounter counterTest;

  @Override
  public void afterPropertiesSet() throws Exception {
    this.counterTest = sdkMeterProvider.meterBuilder("meter-builder").build().counterBuilder("counter-test").setDescription("A test counter").build();
  }

  // Increment test counter
  public void incrementCounter() {
    this.counterTest.add(1);
  }
  
}
```

Sample usage in UsersController:
```java
@GetMapping(WebApiVersions.UsersResourceVersion.ROOT)
public ResponseEntity<UsersDisplayListDto> filterUsers(@PathVariable("tenantUid") String tenantUid,
                                                       @PathVariable("orgUid") String orgUid,
                                                       @RequestParam(value = "filter", required = false) String searchFilter,
                                                       @RequestParam(value = "index", required = false, defaultValue = "1") Integer pageIndex,
                                                       @RequestParam(value = "size", required = false, defaultValue = "10") Integer pageSize,
                                                       @RequestParam(value = "orderBy", required = false, defaultValue = "label") String orderBy) throws FunctionalException {
    SearchFilterDto searchFilterDto = new SearchFilterDto(searchFilter, pageSize, pageIndex, orderBy);
    UsersDisplayListDto users = withSpan(INSTRUMENTATION_NAME, "API_USERS_LIST", (span) -> {
        loggingFacade.infoS(INSTRUMENTATION_NAME, "Find users for tenant [%s], organization [%s], filter [%s]", new Object[]{tenantUid, orgUid, StringUtils.nvl(searchFilter)}, OtelContext.fromSpan(span));
        return userPortService.filterUsers(tenantUid, orgUid, searchFilterDto, span);
    });
    return new ResponseEntity<>(users, HttpStatus.OK);
}
```

To visualize logs in grafana from Loki datasource, navigate to "Explore -> Logs":

![](docs/images/loki_logs.png)

To visualize counters from Tempo datasource, create a new visualization in grafana:

![](docs/images/tempo_counters.png)

Finally, spans are also visible in tempo:

![](docs/images/tempo.png)

![](docs/images/tempo_2.png)

# Keycloak

Latest Keycloak OpenApi: https://www.keycloak.org/docs-api/latest/rest-api/index.html

Postman collection: https://documenter.getpostman.com/view/7294517/SzmfZHnd#0138123a-385c-4f66-8fb5-d26df30247d2

Keycloak endpoints: http://localhost:7080/realms/myrealm/.well-known/openid-configuration

Administration: admin/admin

If client is **confidential**, **client_secret is required**
```shell
curl --location 'http://localhost:7080/realms/myrealm/protocol/openid-connect/token' --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'username=myuser' --data-urlencode 'password=mypass' --data-urlencode 'grant_type=password' --data-urlencode 'client_id=myclient' --data-urlencode 'client_secret=2RTndlQDptvof5fINkrWe7SkDawy1q5I' --data-urlencode 'scope=openid'
```

If client is **public**, client_secret is **not** required
```shell
curl --location 'http://localhost:7080/realms/myrealm/protocol/openid-connect/token' --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'username=myuser' --data-urlencode 'password=mypass' --data-urlencode 'grant_type=password' --data-urlencode 'client_id=myclient' --data-urlencode 'scope=openid'
```

Response:

```json
{
"access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJBc285V0M4cWdVY3JYTnpOYjhVNG14aDczWjdFSGItX0kzZnFYOEpQbERNIn0.eyJleHAiOjE3MzA5MDk4NTcsImlhdCI6MTczMDkwOTU1NywianRpIjoiYWJjY2UwMTgtNGQwYS00OWE4LThiZWQtZTJmZDM2MWU2OTdjIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo3MDgwL3JlYWxtcy9teXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjVjMzkxNmU4LTgyMjUtNGZhMS1hZTc3LWIzYjhkNWQ0NmVlZSIsInR5cCI6IkJlYXJlciIsImF6cCI6Im15Y2xpZW50Iiwic2lkIjoiOWJlYjhmOTAtYWU5Yi00MDA5LTlmYzItMzhiNjE3OTg3M2FkIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW15cmVhbG0iLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6Ik15Rmlyc3ROYW1lIE15TGFzdE5hbWUiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJteXVzZXIiLCJnaXZlbl9uYW1lIjoiTXlGaXJzdE5hbWUiLCJmYW1pbHlfbmFtZSI6Ik15TGFzdE5hbWUiLCJlbWFpbCI6Im15dXNlckB0ZXN0LmZyIn0.Zlr1817cwvWbqjs6KZH08IszIW8yVsfe3oyK9qnFgWHKkJ7FtmVnNskm2J2AU2k-0yFuk7dfCsrKxacNA_RvPZbUUt4qo5I6M0xQonlJdeVZcxKtKJRcuN7eQVaPBjkELc_2dN9w01YwuUTpt5GZeOLrUuDsd31AS--8JA2IPzfD0L1AIEfoTR0lm7oOWvcX7r_2veCpXj-r-oEvO5WSLHBglawJdp8aMQ_oIhW-ICC13_AUzGfzY0lNfiEGS05OJ-gIFl1FHGi_tnmL8Y1lEYn9kBxiEEmgylxsWktVTLZpuOmneTokfT2tkMySfkb1nHIn7AfFIO7jI47X-RCFAA",
"expires_in": 300,
"refresh_expires_in": 1800,
"refresh_token": "eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0NmZjZDU0Yy1iZmMxLTRlOGItYTNiYy0zODU5ZTJjNjk3M2IifQ.eyJleHAiOjE3MzA5MTEzNTcsImlhdCI6MTczMDkwOTU1NywianRpIjoiMjY5NjZhNGYtYTUyOC00M2Y3LTlmNjAtM2Q4MDg5YmY4ODMzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo3MDgwL3JlYWxtcy9teXJlYWxtIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDo3MDgwL3JlYWxtcy9teXJlYWxtIiwic3ViIjoiNWMzOTE2ZTgtODIyNS00ZmExLWFlNzctYjNiOGQ1ZDQ2ZWVlIiwidHlwIjoiUmVmcmVzaCIsImF6cCI6Im15Y2xpZW50Iiwic2lkIjoiOWJlYjhmOTAtYWU5Yi00MDA5LTlmYzItMzhiNjE3OTg3M2FkIiwic2NvcGUiOiJvcGVuaWQgYWNyIHdlYi1vcmlnaW5zIGVtYWlsIGJhc2ljIHJvbGVzIHByb2ZpbGUifQ.CslR6kIsR_Nx8KyUn2k9vuL5_FZ90e9yR1FwiBtONydWxPZNQ8sWJEUFPrU73gZ5EewmeMSQ3EC9Um7BinDVnw",
"token_type": "Bearer",
"id_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJBc285V0M4cWdVY3JYTnpOYjhVNG14aDczWjdFSGItX0kzZnFYOEpQbERNIn0.eyJleHAiOjE3MzA5MDk4NTcsImlhdCI6MTczMDkwOTU1NywianRpIjoiYTUwNzZjMzAtYWVjYS00YWYwLThiZDgtMWYwMzUzNmQzOGVlIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo3MDgwL3JlYWxtcy9teXJlYWxtIiwiYXVkIjoibXljbGllbnQiLCJzdWIiOiI1YzM5MTZlOC04MjI1LTRmYTEtYWU3Ny1iM2I4ZDVkNDZlZWUiLCJ0eXAiOiJJRCIsImF6cCI6Im15Y2xpZW50Iiwic2lkIjoiOWJlYjhmOTAtYWU5Yi00MDA5LTlmYzItMzhiNjE3OTg3M2FkIiwiYXRfaGFzaCI6IjFGMFpHaGY4Sm5SQXZHOFAzRWY2blEiLCJhY3IiOiIxIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiTXlGaXJzdE5hbWUgTXlMYXN0TmFtZSIsInByZWZlcnJlZF91c2VybmFtZSI6Im15dXNlciIsImdpdmVuX25hbWUiOiJNeUZpcnN0TmFtZSIsImZhbWlseV9uYW1lIjoiTXlMYXN0TmFtZSIsImVtYWlsIjoibXl1c2VyQHRlc3QuZnIifQ.HZeC7ihUhAV8-2tv4IBqozeAIz7y6PpUrUgXYYxdmZLwUlrR8g4MhHkiwcc3BgVBXmjGcwjr3HvXZx2Fp_p4rgMQY9JePNFlEor6ynaCgxhF7i7cRUmbFE9kQh83YEsCaSnZwxCTKX0mOWglUx6b29slRyCOkSsqXxFlo4dzISrKq3HP8zLpJ33AEWf8szn6zHf6Ce-gbXHCijRJcTxuSiFKrsSNW_2xKnR6Wpzyh3P9QX-afx3f7acnOyrgAKFZCkgbmmt6yp8stz64oHETGE-cFS_N3JWXS5Wmpp_Ff4cOU__cuzfR9jNFFBoJhdHXwAaX-K3Cg-BOoIL87c9ejA",
"not-before-policy": 0,
"session_state": "9beb8f90-ae9b-4009-9fc2-38b6179873ad",
"scope": "openid email profile"
}
```

You can use website https://jwt.io/ to decode accessToken
