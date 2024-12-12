FROM alpine:3.20.0
ARG JAR_FILE
ARG INITSH
ARG DBSQLFILE
ARG ACTSQLFILE
ARG LIQUITEMP
ARG GRANTSTEMP
ARG GRANTSDBA

COPY ${JAR_FILE} db-migration.jar
COPY ${INITSH} init.sh
COPY ${DBSQLFILE} create_database.sql
COPY ${ACTSQLFILE} accounts_setup.sql
COPY ${LIQUITEMP} liquibase_template.properties
COPY ${GRANTSTEMP} grants_template.sql
COPY ${GRANTSDBA} grants_dba_template.sql

RUN apk add --no-cache postgresql-client openjdk21
    
RUN chmod +x init.sh    

ENV LANG=C.UTF-8 \
    JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
    PATH=$PATH:/usr/lib/jvm/java-21-openjdk/jre/bin:/usr/lib/jvm/java-21-openjdk/bin

ENTRYPOINT [ "/bin/sh", "-c", "./init.sh" ]
