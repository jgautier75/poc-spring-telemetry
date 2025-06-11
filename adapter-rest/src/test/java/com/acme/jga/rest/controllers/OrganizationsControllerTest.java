package com.acme.jga.rest.controllers;

import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationCommonsDto;
import com.acme.jga.ports.dtos.organizations.v1.OrganizationDto;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.services.api.organization.OrganizationPortService;
import com.acme.jga.rest.config.AppDebuggingConfig;
import com.acme.jga.rest.config.AppGenericConfig;
import com.acme.jga.rest.config.MicrometerPrometheus;
import com.acme.jga.rest.config.OpenTelemetryTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganizationsController.class)
@Import(value = {OpenTelemetryTestConfig.class})
class OrganizationsControllerTest {
    private static final String TENANT_UID = UUID.randomUUID().toString();
    @MockitoBean
    private OrganizationPortService organizationPortService;
    @MockitoBean
    private ILoggingFacade loggingFacade;
    @MockitoBean
    private AppGenericConfig appGenericConfig;
    @MockitoBean
    private AppDebuggingConfig appDebuggingConfig;
    @MockitoBean
    private MicrometerPrometheus micrometerPrometheus;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void createOrganization() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        OrganizationCommonsDto orgCommonsDto = OrganizationCommonsDto.builder()
                .code("org-code")
                .country("fr")
                .kind(OrganizationKind.BU)
                .label("org-label")
                .status(OrganizationStatus.ACTIVE)
                .build();

        OrganizationDto organizationDto = OrganizationDto.builder()
                .commons(orgCommonsDto)
                .tenantUid(TENANT_UID)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String orgJson = objectMapper.writeValueAsString(organizationDto);

        // WHEN
        Mockito.when(organizationPortService.createOrganization(Mockito.any(), Mockito.any())).thenReturn(uidDto);

        // THEN
        mockMvc.perform(post("/api/v1/tenants/" + TENANT_UID + "/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orgJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uid", "").exists())
                .andExpect(jsonPath("$.uid", "").value(uidDto.getUid()));
    }

    @Test
    void updateOrganization() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        OrganizationCommonsDto orgCommonsDto = OrganizationCommonsDto.builder()
                .code("org-code")
                .country("fr")
                .kind(OrganizationKind.BU)
                .label("org-label")
                .status(OrganizationStatus.ACTIVE)
                .build();

        OrganizationDto organizationDto = OrganizationDto.builder()
                .commons(orgCommonsDto)
                .tenantUid(TENANT_UID)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String orgJson = objectMapper.writeValueAsString(organizationDto);

        // WHEN
        Mockito.when(organizationPortService.updateOrganization(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(1);
        // THEN
        String targetUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + uidDto.getUid();
        mockMvc.perform(post(targetUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orgJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrganization() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());

        // WHEN
        Mockito.when(organizationPortService.deleteOrganization(Mockito.any(), Mockito.any())).thenReturn(1);

        // THEN
        String targetUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + uidDto.getUid();
        mockMvc.perform(delete(targetUri)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

}
