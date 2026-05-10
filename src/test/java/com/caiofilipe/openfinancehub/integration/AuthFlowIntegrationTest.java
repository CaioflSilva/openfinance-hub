package com.caiofilipe.openfinancehub.integration;

import com.caiofilipe.openfinancehub.IntegrationTest;
import com.caiofilipe.openfinancehub.dto.request.LoginRequest;
import com.caiofilipe.openfinancehub.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthFlowIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private RegisterRequest uniqueRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Integration User");
        req.setEmail("auth-" + UUID.randomUUID() + "@test.com");
        req.setPassword("senha123");
        return req;
    }

    @Test
    void register_validRequest_returns200WithJwt() throws Exception {
        RegisterRequest req = uniqueRegisterRequest();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(req.getEmail()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_validCredentials_returns200WithJwt() throws Exception {
        RegisterRequest reg = uniqueRegisterRequest();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setEmail(reg.getEmail());
        login.setPassword(reg.getPassword());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(reg.getEmail()));
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/bank-accounts"))
                .andExpect(status().isUnauthorized());
    }
}
