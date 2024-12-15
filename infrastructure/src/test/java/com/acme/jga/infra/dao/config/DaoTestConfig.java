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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    public ExpressionsProcessor expressionsProcessor(){
        return new ExpressionsProcessor();
    }

    @Bean
    public ITenantsDao tenantsDao(@Autowired DataSource dataSource,
                                  @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new TenantsDao(dataSource, namedParameterJdbcTemplate);
    }

    @Bean
    public IOrganizationsDao organizationsDao(@Autowired DataSource dataSource,
                                              @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                              @Autowired ExpressionsProcessor expressionsProcessor) {
        return new OrganizationsDao(dataSource, namedParameterJdbcTemplate, expressionsProcessor);
    }

    @Bean
    public IUsersDao usersDao(@Autowired DataSource dataSource,
                              @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                              @Autowired ExpressionsProcessor expressionsProcessor) {
        return new UsersDao(dataSource, namedParameterJdbcTemplate, expressionsProcessor);
    }

    @Bean
    public ISectorsDao sectorsDao(@Autowired DataSource dataSource,
                                  @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new SectorsDao(dataSource, namedParameterJdbcTemplate);
    }

    @Bean
    public AuditEventsInfraConverter auditEventsInfraConverter() {
        return new AuditEventsInfraConverter();
    }

    @Bean
    public IEventsDao eventsDao(@Autowired DataSource dataSource,
                                @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new EventsDao(dataSource, namedParameterJdbcTemplate);
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
}
