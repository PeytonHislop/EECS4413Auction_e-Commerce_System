package com.code2cash.auction.controller;

import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.service.BidService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for BidController
 * Tests bid placement and retrieval functionality
 */
@WebMvcTest(BidController.class)
public class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BidService bidService;

    private BidResponse mockBidResponse;
    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
        
        mockBidResponse = new BidResponse();
        mockBidResponse.setBidId("BID001");
        mockBidResponse.setAuctionId("AUC001");
        mockBidResponse.setBidAmount(100.0);
        mockBidResponse.setBidderId("BUYER001");
        mockBidResponse.setCreatedAt(LocalDateTime.now());
    }

    // GET BID HISTORY TESTS
    @Test
    @DisplayName("GET /api/auctions/{auctionId}/bids - Get bid history for auction")
    void testGetBidHistory_WithValidAuctionId_ReturnsBids() throws Exception {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("BID001");
        bid1.setAuctionId("AUC001");
        bid1.setBidAmount(BigDecimal.valueOf(100.00));
        
        Bid bid2 = new Bid();
        bid2.setBidId("BID002");
        bid2.setAuctionId("AUC001");
        bid2.setBidAmount(BigDecimal.valueOf(150.00));
        
        List<Bid> bids = Arrays.asList(bid1, bid2);
        when(bidService.getBidHistory("AUC001")).thenReturn(bids);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC001/bids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].bidId").value("BID001"))
                .andExpect(jsonPath("$[1].bidId").value("BID002"));

        verify(bidService).getBidHistory("AUC001");
    }

    @Test
    @DisplayName("GET /api/auctions/{auctionId}/bids - No bids returns empty list")
    void testGetBidHistory_NoBids_ReturnsEmptyList() throws Exception {
        // Arrange
        when(bidService.getBidHistory("AUC999")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC999/bids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bidService).getBidHistory("AUC999");
    }

    // PLACE BID TESTS 
    @Test
    @DisplayName("POST /api/auctions/{auctionId}/bids - Place bid with valid token")
    void testPlaceBid_WithValidToken_ReturnsCreatedBid() throws Exception {
        // Arrange
        BidResponse response = new BidResponse();
        response.setBidId("BID001");
        response.setAuctionId("AUC001");
        response.setBidAmount(150.0);
        response.setSuccessful(true);
        
        when(bidService.placeBid(eq("AUC001"), anyDouble(), anyString()))
                .thenReturn(response);

        // Act & Assert
        String bidJson = "{\"bidAmount\": 150.0}";
        mockMvc.perform(post("/api/auctions/AUC001/bids")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bidJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bidId").value("BID001"))
                .andExpect(jsonPath("$.bidAmount").value(150.0))
                .andExpect(jsonPath("$.successful").value(true));

        verify(bidService).placeBid(eq("AUC001"), anyDouble(), anyString());
    }

    @Test
    @DisplayName("POST /api/auctions/{auctionId}/bids - Place bid without auth header")
    void testPlaceBid_WithoutAuthHeader_ReturnsBadRequest() throws Exception {
        // Act & Assert
        String bidJson = "{\"bidAmount\": 150.0}";
        mockMvc.perform(post("/api/auctions/AUC001/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auctions/{auctionId}/bids - Bid too low returns failure")
    void testPlaceBid_BidTooLow_ReturnsFailed() throws Exception {
        // Arrange
        BidResponse response = new BidResponse();
        response.setBidId("BID001");
        response.setAuctionId("AUC001");
        response.setBidAmount(50.0);
        response.setSuccessful(false);
        response.setMessage("Bid must be higher than current bid");
        
        when(bidService.placeBid(eq("AUC001"), eq(50.0), anyString()))
                .thenReturn(response);

        // Act & Assert
        String bidJson = "{\"bidAmount\": 50.0}";
        mockMvc.perform(post("/api/auctions/AUC001/bids")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bidJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.message").value(containsString("higher")));
    }

    @Test
    @DisplayName("POST /api/auctions/{auctionId}/bids - Place bid on closed auction fails")
    void testPlaceBid_AuctionClosed_ReturnsFailed() throws Exception {
        // Arrange
        BidResponse response = new BidResponse();
        response.setSuccessful(false);
        response.setMessage("Auction is closed");
        
        when(bidService.placeBid(eq("AUC001"), anyDouble(), anyString()))
                .thenReturn(response);

        // Act & Assert
        String bidJson = "{\"bidAmount\": 150.0}";
        mockMvc.perform(post("/api/auctions/AUC001/bids")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bidJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(false));
    }

    // GET HIGHEST BID TESTS 
    @Test
    @DisplayName("GET /api/auctions/{auctionId}/highest-bid - Get highest bid")
    void testGetHighestBid_ReturnsHighestBid() throws Exception {
        // Arrange
        Bid highestBid = new Bid();
        highestBid.setBidId("BID002");
        highestBid.setAuctionId("AUC001");
        highestBid.setBidAmount(BigDecimal.valueOf(200.00));
        highestBid.setBidderId("BUYER002");
        
        when(bidService.getHighestBid("AUC001")).thenReturn(highestBid);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC001/highest-bid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidId").value("BID002"))
                .andExpect(jsonPath("$.bidAmount").value(200.00))
                .andExpect(jsonPath("$.bidderId").value("BUYER002"));

        verify(bidService).getHighestBid("AUC001");
    }

    @Test
    @DisplayName("GET /api/auctions/{auctionId}/highest-bid - No bids returns null")
    void testGetHighestBid_NoBids_ReturnsNull() throws Exception {
        // Arrange
        when(bidService.getHighestBid("AUC999")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC999/highest-bid"))
                .andExpect(status().isNoContent());

        verify(bidService).getHighestBid("AUC999");
    }
}
