package com.code2cash.auction.controller;

import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.dto.CreateAuctionRequest;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for AuctionController
 * Tests REST endpoints with various success and failure scenarios
 */
@WebMvcTest(AuctionController.class)
public class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionService auctionService;

    private CreateAuctionRequest createRequest;
    private AuctionResponse mockAuctionResponse;
    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
        
        createRequest = new CreateAuctionRequest();
        createRequest.setItemId("ITEM001");
        createRequest.setStartPrice(100.0);
        createRequest.setDurationHours(24);
        
        mockAuctionResponse = new AuctionResponse();
        mockAuctionResponse.setAuctionId("AUC001");
        mockAuctionResponse.setItemId("ITEM001");
        mockAuctionResponse.setStatus(AuctionStatus.ACTIVE);
        mockAuctionResponse.setCurrentBid(100.0);
        mockAuctionResponse.setStartPrice(100.0);
        mockAuctionResponse.setCreatedAt(LocalDateTime.now());
    }

    // ========== CREATE AUCTION TESTS ==========
    @Test
    @DisplayName("POST /api/auctions - Create auction with valid token")
    void testCreateAuction_WithValidToken_ReturnsCreated() throws Exception {
        // Arrange
        when(auctionService.createAuction(any(CreateAuctionRequest.class), anyString()))
                .thenReturn(mockAuctionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auctions")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auctionId").value("AUC001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(auctionService).createAuction(any(CreateAuctionRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /api/auctions - Create auction without auth header")
    void testCreateAuction_WithoutAuthHeader_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auctions - Create auction with invalid request body")
    void testCreateAuction_WithInvalidBody_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{ invalid json }";

        // Act & Assert
        mockMvc.perform(post("/api/auctions")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ========== GET AUCTION TESTS ==========
    @Test
    @DisplayName("GET /api/auctions/{id} - Get auction by ID")
    void testGetAuction_WithValidId_ReturnsAuction() throws Exception {
        // Arrange
        when(auctionService.getAuction("AUC001")).thenReturn(mockAuctionResponse);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctionId").value("AUC001"))
                .andExpect(jsonPath("$.itemId").value("ITEM001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(auctionService).getAuction("AUC001");
    }

    @Test
    @DisplayName("GET /api/auctions/{id} - Get non-existent auction returns null")
    void testGetAuction_WithInvalidId_ReturnsNull() throws Exception {
        // Arrange
        when(auctionService.getAuction("INVALID")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/INVALID"))
                .andExpect(status().isOk())
                .andExpect(content().string("null"));

        verify(auctionService).getAuction("INVALID");
    }

    // ========== GET ACTIVE AUCTIONS TESTS ==========
    @Test
    @DisplayName("GET /api/auctions/active - Get all active auctions")
    void testGetActiveAuctions_ReturnsOk() throws Exception {
        // Arrange
        AuctionResponse auction1 = new AuctionResponse();
        auction1.setAuctionId("AUC001");
        auction1.setStatus(AuctionStatus.ACTIVE);
        
        AuctionResponse auction2 = new AuctionResponse();
        auction2.setAuctionId("AUC002");
        auction2.setStatus(AuctionStatus.ACTIVE);
        
        List<AuctionResponse> auctions = Arrays.asList(auction1, auction2);
        when(auctionService.getActiveAuctions()).thenReturn(auctions);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].auctionId").value("AUC001"))
                .andExpect(jsonPath("$[1].auctionId").value("AUC002"));

        verify(auctionService).getActiveAuctions();
    }

    @Test
    @DisplayName("GET /api/auctions/active - Empty list when no active auctions")
    void testGetActiveAuctions_NoActiveAuctions_ReturnsEmptyList() throws Exception {
        // Arrange
        when(auctionService.getActiveAuctions()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/auctions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(auctionService).getActiveAuctions();
    }

    // ========== GET SELLER AUCTIONS TESTS ==========
    @Test
    @DisplayName("GET /api/auctions/seller/{sellerId} - Get seller's auctions")
    void testGetSellerAuctions_WithValidSellerId_ReturnsSellerAuctions() throws Exception {
        // Arrange
        AuctionResponse auction = new AuctionResponse();
        auction.setAuctionId("AUC001");
        auction.setSellerId("SELLER001");
        
        List<AuctionResponse> auctions = Arrays.asList(auction);
        when(auctionService.getSellerAuctions("SELLER001")).thenReturn(auctions);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/seller/SELLER001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sellerId").value("SELLER001"));

        verify(auctionService).getSellerAuctions("SELLER001");
    }

    @Test
    @DisplayName("GET /api/auctions/seller/{sellerId} - No auctions for seller returns empty")
    void testGetSellerAuctions_NoAuctions_ReturnsEmptyList() throws Exception {
        // Arrange
        when(auctionService.getSellerAuctions("SELLER999")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/auctions/seller/SELLER999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ========== CLOSE AUCTION TESTS ==========
    @Test
    @DisplayName("PUT /api/auctions/{id}/close - Close auction")
    void testCloseAuction_WithValidId_ReturnsClosedAuction() throws Exception {
        // Arrange
        AuctionResponse closedAuction = new AuctionResponse();
        closedAuction.setAuctionId("AUC001");
        closedAuction.setStatus(AuctionStatus.CLOSED);
        
        when(auctionService.closeAuction("AUC001")).thenReturn(closedAuction);

        // Act & Assert
        mockMvc.perform(put("/api/auctions/AUC001/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctionId").value("AUC001"))
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(auctionService).closeAuction("AUC001");
    }

    // ========== CLOSE EXPIRED AUCTIONS TESTS ==========
    @Test
    @DisplayName("POST /api/auctions/close-expired - Close expired auctions")
    void testCloseExpiredAuctions_ReturnsCount() throws Exception {
        // Arrange
        when(auctionService.closeExpiredAuctions()).thenReturn(5);

        // Act & Assert
        mockMvc.perform(post("/api/auctions/close-expired"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("5")));

        verify(auctionService).closeExpiredAuctions();
    }

    @Test
    @DisplayName("POST /api/auctions/close-expired - No expired auctions returns 0")
    void testCloseExpiredAuctions_NoExpired_ReturnsZero() throws Exception {
        // Arrange
        when(auctionService.closeExpiredAuctions()).thenReturn(0);

        // Act & Assert
        mockMvc.perform(post("/api/auctions/close-expired"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0")));
    }
}
