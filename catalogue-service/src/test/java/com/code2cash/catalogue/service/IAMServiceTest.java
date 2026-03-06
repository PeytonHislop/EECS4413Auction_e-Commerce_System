package com.code2cash.catalogue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.code2cash.catalogue.config.IAMServiceConfig;
import com.code2cash.catalogue.dto.AuthorizeRequest;
import com.code2cash.catalogue.dto.ValidateTokenResponse;

/**
 * Unit tests for IAMService
 * Tests IAM integration service methods
 */
@ExtendWith(MockitoExtension.class)
class IAMServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private IAMServiceConfig iamServiceConfig;

    @InjectMocks
    private IAMService iamService;

    private String validToken;
    private String iamServiceUrl;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
        iamServiceUrl = "http://localhost:8080";
        when(iamServiceConfig.getIamServiceUrl()).thenReturn(iamServiceUrl);
    }

    // ========== validateToken Tests ==========

    @Test
    @DisplayName("validateToken - Return valid response for valid token")
    void testValidateToken_WithValidToken_ReturnsValidResponse() {
        // Arrange
        ValidateTokenResponse expectedResponse = new ValidateTokenResponse(true, 123L, "testuser", "SELLER");
        ResponseEntity<ValidateTokenResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ValidateTokenResponse.class)))
                .thenReturn(responseEntity);

        // Act
        ValidateTokenResponse result = iamService.validateToken(validToken);

        // Assert
        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(123L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("SELLER", result.getRole());

        // Verify correct URL was called
        verify(restTemplate).exchange(
                eq(iamServiceUrl + "/auth/validate"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ValidateTokenResponse.class));
    }

    @Test
    @DisplayName("validateToken - Set Authorization header correctly")
    void testValidateToken_SetsAuthorizationHeader() {
        // Arrange
        ValidateTokenResponse expectedResponse = new ValidateTokenResponse(true, 123L, "testuser", "SELLER");
        ResponseEntity<ValidateTokenResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ValidateTokenResponse.class)))
                .thenReturn(responseEntity);

        // Act
        iamService.validateToken(validToken);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(ValidateTokenResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(validToken, headers.getFirst("Authorization"));
    }

    @Test
    @DisplayName("validateToken - Return invalid response on RestClientException")
    void testValidateToken_WithRestClientException_ReturnsInvalidResponse() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ValidateTokenResponse.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Act
        ValidateTokenResponse result = iamService.validateToken(validToken);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNull(result.getUserId());
        assertNull(result.getUsername());
        assertNull(result.getRole());
    }

    @Test
    @DisplayName("validateToken - Return invalid response on generic exception")
    void testValidateToken_WithGenericException_ReturnsInvalidResponse() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ValidateTokenResponse.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ValidateTokenResponse result = iamService.validateToken(validToken);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNull(result.getUserId());
    }

    @Test
    @DisplayName("validateToken - Handle null response body")
    void testValidateToken_WithNullResponseBody_ReturnsNull() {
        // Arrange
        ResponseEntity<ValidateTokenResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ValidateTokenResponse.class)))
                .thenReturn(responseEntity);

        // Act
        ValidateTokenResponse result = iamService.validateToken(validToken);

        // Assert
        assertNull(result);
    }

    // ========== authorizeRole Tests ==========

    @Test
    @DisplayName("authorizeRole - Return true when authorized")
    void testAuthorizeRole_WithAuthorizedRole_ReturnsTrue() {
        // Arrange
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = iamService.authorizeRole(validToken, "SELLER");

        // Assert
        assertTrue(result);
        verify(restTemplate).exchange(
                eq(iamServiceUrl + "/auth/authorize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class));
    }

    @Test
    @DisplayName("authorizeRole - Return false when not authorized")
    void testAuthorizeRole_WithUnauthorizedRole_ReturnsFalse() {
        // Arrange
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(false, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = iamService.authorizeRole(validToken, "ADMIN");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("authorizeRole - Send correct request body with required role")
    void testAuthorizeRole_SendsCorrectRequestBody() {
        // Arrange
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class)))
                .thenReturn(responseEntity);

        // Act
        iamService.authorizeRole(validToken, "SELLER");

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Boolean.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertTrue(capturedEntity.getBody() instanceof AuthorizeRequest);
        AuthorizeRequest request = (AuthorizeRequest) capturedEntity.getBody();
        assertEquals("SELLER", request.getRole());
    }

    @Test
    @DisplayName("authorizeRole - Set correct headers")
    void testAuthorizeRole_SetsCorrectHeaders() {
        // Arrange
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class)))
                .thenReturn(responseEntity);

        // Act
        iamService.authorizeRole(validToken, "SELLER");

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Boolean.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(validToken, headers.getFirst("Authorization"));
        assertTrue(headers.getContentType().toString().contains("application/json"));
    }

    @Test
    @DisplayName("authorizeRole - Return false on exception")
    void testAuthorizeRole_WithException_ReturnsFalse() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Act
        boolean result = iamService.authorizeRole(validToken, "SELLER");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("authorizeRole - Handle null response body as false")
    void testAuthorizeRole_WithNullResponseBody_ReturnsFalse() {
        // Arrange
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = iamService.authorizeRole(validToken, "SELLER");

        // Assert
        assertFalse(result);
    }

    // ========== getUserInfo Tests ==========

    @Test
    @DisplayName("getUserInfo - Return user info when found")
    void testGetUserInfo_WithValidUserId_ReturnsUserInfo() {
        // Arrange
        Object expectedUserInfo = new Object(); // Mock user info
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(expectedUserInfo, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(responseEntity);

        // Act
        Object result = iamService.getUserInfo(123L);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUserInfo, result);
        verify(restTemplate).getForEntity(
                eq(iamServiceUrl + "/users/123"),
                eq(Object.class));
    }

    @Test
    @DisplayName("getUserInfo - Return null on exception")
    void testGetUserInfo_WithException_ReturnsNull() {
        // Arrange
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenThrow(new RestClientException("User not found"));

        // Act
        Object result = iamService.getUserInfo(123L);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("getUserInfo - Use correct URL with userId")
    void testGetUserInfo_UsesCorrectUrl() {
        // Arrange
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(new Object(), HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(responseEntity);

        // Act
        iamService.getUserInfo(456L);

        // Assert
        verify(restTemplate).getForEntity(
                eq(iamServiceUrl + "/users/456"),
                eq(Object.class));
    }

    @Test
    @DisplayName("getUserInfo - Handle null response body")
    void testGetUserInfo_WithNullResponseBody_ReturnsNull() {
        // Arrange
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(responseEntity);

        // Act
        Object result = iamService.getUserInfo(123L);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("authorizeRole - Test with different role names")
    void testAuthorizeRole_WithDifferentRoles() {
        // Test BUYER role
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Boolean.class)))
                .thenReturn(responseEntity);

        boolean result = iamService.authorizeRole(validToken, "BUYER");
        assertTrue(result);

        // Verify request contains BUYER role
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Boolean.class));

        AuthorizeRequest request = (AuthorizeRequest) entityCaptor.getValue().getBody();
        assertEquals("BUYER", request.getRole());
    }
}
