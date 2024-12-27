package com.acme.jga.rest.controllers;

import com.acme.jga.logging.services.api.ILogService;
import com.acme.jga.ports.port.shared.UidDto;
import com.acme.jga.ports.port.tenants.v1.TenantDto;
import com.acme.jga.ports.services.api.tenant.ITenantPortService;
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

@WebMvcTest(controllers = TenantsController.class)
@Import(value = {OpenTelemetryTestConfig.class})
class TenantsControllerTest {
    @MockitoBean
    private ITenantPortService tenantPortService;
    @MockitoBean
    private ILogService logService;
    @MockitoBean
    private MicrometerPrometheus micrometerPrometheus;
    @MockitoBean
    private AppGenericConfig appGenericConfig;
    @MockitoBean
    private AppDebuggingConfig appDebuggingConfig;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;


    @Test
    void createTenant() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        TenantDto tenantDto = new TenantDto("tenant-code", "tenant-label");
        ObjectMapper mapper = new ObjectMapper();
        String tenantJson = mapper.writeValueAsString(tenantDto);

        // WHEN
        Mockito.when(tenantPortService.createTenant(Mockito.any())).thenReturn(uidDto);

        // THEN
        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantJson)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uid", "").exists())
                .andExpect(jsonPath("$.uid", "").value(uidDto.getUid()));
    }

    @Test
    void updateTenant() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        TenantDto tenantDto = new TenantDto("tenant-code", "tenant-label");
        ObjectMapper mapper = new ObjectMapper();
        String tenantJson = mapper.writeValueAsString(tenantDto);

        // WHEN
        Mockito.when(tenantPortService.updateTenant(Mockito.any(), Mockito.any())).thenReturn(1);

        // THEN
        String targetUri = "/api/v1/tenants/" + uidDto.getUid();
        mockMvc.perform(post(targetUri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantJson)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTenant() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());

        // WHEN
        Mockito.when(tenantPortService.deleteTenant(Mockito.any())).thenReturn(1);

        // THEN
        String targetUri = "/api/v1/tenants/" + uidDto.getUid();
        mockMvc.perform(delete(targetUri)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

}
