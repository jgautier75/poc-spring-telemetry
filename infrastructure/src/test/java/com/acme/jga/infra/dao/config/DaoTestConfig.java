package com.acme.jga.infra.dao.config;

import com.acme.jga.infra.converters.AuditEventsInfraConverter;
import com.acme.jga.infra.dao.api.events.EventsDao;
import com.acme.jga.infra.dao.api.organizations.OrganizationsDao;
import com.acme.jga.infra.dao.api.sectors.SectorsDao;
import com.acme.jga.infra.dao.api.tenants.TenantsDao;
import com.acme.jga.infra.dao.api.users.UsersDao;
import com.acme.jga.infra.dao.impl.events.EventsDaoImpl;
import com.acme.jga.infra.dao.impl.organizations.OrganizationsDaoImpl;
import com.acme.jga.infra.dao.impl.sectors.SectorsDaoImpl;
import com.acme.jga.infra.dao.impl.tenants.TenantsDaoImpl;
import com.acme.jga.infra.dao.impl.users.UsersDaoImpl;
import com.acme.jga.infra.dao.processors.ExpressionsProcessor;
import com.acme.jga.infra.utils.DummyMessageSource;
import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.logging.services.api.IOtelLogService;
import com.acme.jga.logging.services.impl.LogService;
import com.acme.jga.logging.services.impl.LoggingFacade;
import com.acme.jga.logging.services.impl.OtelLogService;
import com.acme.jga.opentelemetry.OpenTelemetryWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.time.ZoneOffset;
import java.util.TimeZone;

@Configuration
public class DaoTestConfig {

    @Bean
    public ExpressionsProcessor expressionsProcessor() {
        return new ExpressionsProcessor();
    }

    @Bean
    public TenantsDao tenantsDao(@Autowired DataSource dataSource,
                                 @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 @Autowired OpenTelemetryWrapper openTelemetryWrapper) {
        return new TenantsDaoImpl(dataSource, namedParameterJdbcTemplate, openTelemetryWrapper);
    }

    @Bean
    public OrganizationsDao organizationsDao(@Autowired DataSource dataSource,
                                             @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                             @Autowired ExpressionsProcessor expressionsProcessor,
                                             @Autowired OpenTelemetryWrapper openTelemetryWrapper) {
        return new OrganizationsDaoImpl(dataSource, namedParameterJdbcTemplate, expressionsProcessor, openTelemetryWrapper);
    }

    @Bean
    public UsersDao usersDao(@Autowired DataSource dataSource,
                             @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                             @Autowired ExpressionsProcessor expressionsProcessor,
                             @Autowired OpenTelemetryWrapper openTelemetryWrapper,
                             @Autowired ILoggingFacade loggingFacade) {
        return new UsersDaoImpl(dataSource, namedParameterJdbcTemplate, expressionsProcessor, openTelemetryWrapper, loggingFacade);
    }

    @Bean
    public SectorsDao sectorsDao(@Autowired DataSource dataSource,
                                 @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 @Autowired OpenTelemetryWrapper openTelemetryWrapper,
                                 @Autowired ILoggingFacade loggingFacade) {
        return new SectorsDaoImpl(dataSource, namedParameterJdbcTemplate, openTelemetryWrapper, loggingFacade);
    }

    @Bean
    public AuditEventsInfraConverter auditEventsInfraConverter() {
        return new AuditEventsInfraConverter();
    }

    @Bean
    public EventsDao eventsDao(@Autowired DataSource dataSource,
                               @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                               @Autowired OpenTelemetryWrapper openTelemetryWrapper) {
        return new EventsDaoImpl(dataSource, namedParameterJdbcTemplate, openTelemetryWrapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .configure(SerializationFeature.INDENT_OUTPUT, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        return objectMapper;
    }

    @Bean
    public OpenTelemetryWrapper openTelemetryWrapper() {
        return new OpenTelemetryWrapper();
    }

    @Bean
    public ILogService logService() {
        return new LogService(new DummyMessageSource());
    }

    @Bean
    public IOtelLogService otelLogService() {
        return new OtelLogService(LoggerProvider.noop());
    }

    @Bean
    public ILoggingFacade loggingFacade(@Autowired ILogService logService, @Autowired IOtelLogService otelLogService) {
        return new LoggingFacade(logService, otelLogService);
    }

    @Bean
    public TracerProvider tracerProvider() {
        return TracerProvider.noop();
    }

    @Bean
    public MeterProvider meterProvider() {
        return MeterProvider.noop();
    }

}
