package com.yorku.eecs4413.platform.iam_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String signupPayload(String username, String email) {
        return """
            {
              "username": "%s",
              "password": "P@ssword123",
              "firstName": "Ravi",
              "lastName": "Deol",
              "email": "%s",
              "role": "BUYER",
              "shippingAddress": {
                "streetNumber": "123",
                "streetName": "Main St",
                "city": "Brampton",
                "country": "Canada",
                "postalCode": "L6X1X1"
              }
            }
            """.formatted(username, email);
    }

    @Test
    void signup_success() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupPayload("ravi_test_1", "ravi_test_1@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", not(blankOrNullString())))
                .andExpect(jsonPath("$.username").value("ravi_test_1"))
                .andExpect(jsonPath("$.role").value("BUYER"));
    }

    @Test
    void signup_duplicateUsername_fails() throws Exception {
        String payload1 = signupPayload("duplicate_user", "dup1@example.com");
        String payload2 = signupPayload("duplicate_user", "dup2@example.com");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Username already exists")));
    }

    @Test
    void login_success_returnsToken() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupPayload("login_user", "login_user@example.com")))
                .andExpect(status().isCreated());

        String loginReq = """
            {
              "username": "login_user",
              "password": "P@ssword123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("login_user"));
    }

    @Test
    void login_wrongPassword_fails() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupPayload("wrong_pw_user", "wrong_pw_user@example.com")))
                .andExpect(status().isCreated());

        String badLogin = """
            {
              "username": "wrong_pw_user",
              "password": "BadPassword!"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid username or password")));
    }

    @Test
    void validateToken_success() throws Exception {
        // signup
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupPayload("validate_user", "validate_user@example.com")))
                .andExpect(status().isCreated());

        // login
        String loginReq = """
            {
              "username": "validate_user",
              "password": "P@ssword123"
            }
            """;

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginReq))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(loginResponse);
        String token = node.get("token").asText();

        mockMvc.perform(post("/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("validate_user"))
                .andExpect(jsonPath("$.role").value("BUYER"));
    }

    @Test
    void signup_missingFields_validationFails() throws Exception {
        String badSignup = """
            {
              "username": "",
              "password": "short",
              "firstName": "",
              "lastName": "Deol",
              "email": "not-an-email"
            }
            """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badSignup))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}