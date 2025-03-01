package com.acme.jga.infra.dao.config;

import com.acme.jga.infra.converters.AuditEventsInfraConverter;
import com.acme.jga.infra.dao.api.events.IEventsDao;
import com.acme.jga.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.jga.infra.dao.api.sectors.ISectorsDao;
import com.acme.jga.infra.dao.api.tenants.ITenantsDao;
import com.acme.jga.infra.dao.api.users.IUsersDao;
import com.acme.jga.infra.dao.impl.events.EventsDao;
import com.acme.jga.infra.dao.impl.organizations.OrganizationsDao;
import com.acme.jga.infra.dao.impl.sectors.SectorsDao;
import com.acme.jga.infra.dao.impl.tenants.TenantsDao;
import com.acme.jga.infra.dao.impl.users.UsersDao;
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
import io.opentelemetry.api.incubator.logs.ExtendedDefaultLoggerProvider;
import io.opentelemetry.api.incubator.metrics.ExtendedDefaultMeterProvider;
import io.opentelemetry.api.incubator.trace.ExtendedDefaultTracerProvider;
import io.opentelemetry.api.incubator.trace.ExtendedTracer;
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
    public ITenantsDao tenantsDao(@Autowired DataSource dataSource,
                                  @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                  @Autowired OpenTelemetryWrapper openTelemetryWrapper) {
        return new TenantsDao(dataSource, namedParameterJdbcTemplate, openTelemetryWrapper);
    }

    @Bean
    public IOrganizationsDao organizationsDao(@Autowired DataSource dataSource,
                                              @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                              @Autowired ExpressionsProcessor expressionsProcessor,
                                              @Autowired OpenTelemetryWrapper openTelemetryWrapper) {
        return new OrganizationsDao(dataSource, namedParameterJdbcTemplate, expressionsProcessor, openTelemetryWrapper);
    }

    @Bean
    public IUsersDao usersDao(@Autowired DataSource dataSource,
                              @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                              @Autowired ExpressionsProcessor expressionsProcessor,
                              @Autowired OpenTelemetryWrapper openTelemetryWrapper,
                              @Autowired ILoggingFacade loggingFacade) {
        return new UsersDao(dataSource, namedParameterJdbcTemplate, expressionsProcessor, openTelemetryWrapper, loggingFacade);
    }

    @Bean
    public ISectorsDao sectorsDao(@Autowired DataSource dataSource,
                                  @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                  @Autowired OpenTelemetryWrapper openTelemetryWrapper,
                                  @Autowired ILoggingFacade loggingFacade) {
        return new SectorsDao(dataSource, namedParameterJdbcTemplate, openTelemetryWrapper, loggingFacade);
    }

    @Bean
    public AuditEventsInfraConverter auditEventsInfraConverter() {
        return new AuditEventsInfraConverter();
    }

    @Bean
    public IEventsDao eventsDao(@Autowired DataSource dataSource,
                                @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                @Autowired OpenTelemetryWrapper openTelemetryWrapper) {
        return new EventsDao(dataSource, namedParameterJdbcTemplate, openTelemetryWrapper);
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
        return new OtelLogService(ExtendedDefaultLoggerProvider.getNoop());
    }

    @Bean
    public ILoggingFacade loggingFacade(@Autowired ILogService logService, @Autowired IOtelLogService otelLogService) {
        return new LoggingFacade(logService, otelLogService);
    }

    @Bean
    public TracerProvider tracerProvider(){
        return ExtendedDefaultTracerProvider.getNoop();
    }

    @Bean
    public MeterProvider meterProvider(){
        return ExtendedDefaultMeterProvider.getNoop();
    }

}
