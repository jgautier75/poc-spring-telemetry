FROM eclipse-temurin:21.0.3_9-jdk-alpine AS customjre
ARG JAR_FILE

WORKDIR /app
RUN apk add --no-cache unzip
COPY ${JAR_FILE} app.jar
RUN jlink --compress=2 --no-header-files --no-man-pages --add-modules java.base,java.desktop,java.instrument,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql.rowset,jdk.compiler,jdk.jfr,jdk.management,jdk.unsupported,jdk.crypto.ec  --output /app/customjre

FROM alpine:3.20.0
EXPOSE 8080/tcp
RUN mkdir -p /app/ && apk add --no-cache curl jq
COPY --from=customjre /app/customjre /app/customjre
COPY --from=customjre /app/app.jar /app/app.jar
ENV JAVA_HOME=/app/customjre
CMD ["/app/customjre/bin/java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]