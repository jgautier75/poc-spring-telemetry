package com.acme.jga.rest.controllers;

import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.ports.dtos.shared.UidDto;
import com.acme.jga.ports.dtos.users.v1.*;
import com.acme.jga.ports.services.api.users.UserPortService;
import com.acme.jga.rest.config.AppDebuggingProperties;
import com.acme.jga.rest.config.AppGenericProperties;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsersController.class)
@Import(value = {OpenTelemetryTestConfig.class})
class UsersControllerTest {
    private static final String TENANT_UID = UUID.randomUUID().toString();
    private static final String ORG_UID = UUID.randomUUID().toString();
    @MockitoBean
    private UserPortService userPortService;
    @MockitoBean
    private ILoggingFacade loggingFacade;
    @MockitoBean
    private AppGenericProperties appGenericProperties;
    @MockitoBean
    private AppDebuggingProperties appDebuggingProperties;
    @MockitoBean
    private MicrometerPrometheus micrometerPrometheus;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void createUser() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        UserDto userDto = mockUserDto();

        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(userDto);

        // WHEN
        Mockito.when(userPortService.createUser(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(uidDto);

        // THEN
        String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users";
        mockMvc.perform(post(usersUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uid", "").exists())
                .andExpect(jsonPath("$.uid", "").value(uidDto.getUid()));
    }

    @Test
    void updateUser() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        UserDto userDto = mockUserDto();

        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(userDto);

        // WHEN
        Mockito.when(userPortService.createUser(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(uidDto);

        // THEN
        String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users/"
                + uidDto.getUid();
        mockMvc.perform(post(usersUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void listUsers() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        UserDisplayDto userDisplayDto = UserDisplayDto.builder()
                .email("test.test@test.fr")
                .firstName("fname")
                .lastName("lname")
                .login("tlogin")
                .uid(uidDto.getUid())
                .build();
        List<UserDisplayDto> usersList = List.of(userDisplayDto);
        UsersDisplayListDto usersDisplayListDto = new UsersDisplayListDto(0, 0, 0, 0, usersList);

        // WHEN
        Mockito.when(userPortService.filterUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(usersDisplayListDto);

        // THEN
        String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users";
        mockMvc.perform(get(usersUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser() throws Exception {

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());

        // WHEN
        Mockito.when(userPortService.deleteUser(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);

        // THEN
        String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users/"
                + uidDto.getUid();
        mockMvc.perform(delete(usersUri)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * Mock user dto.
     *
     * @return User DTO
     */
    private UserDto mockUserDto() {
        UserCommonsDto userCommonsDto = UserCommonsDto.builder()
                .firstName("fname")
                .lastName("lname")
                .middleName("mname")
                .build();
        UserCredentialsDto userCredentialsDto = UserCredentialsDto.builder()
                .email("email")
                .login("login")
                .build();
        return UserDto.builder()
                .commons(userCommonsDto)
                .credentials(userCredentialsDto)
                .build();
    }

}
