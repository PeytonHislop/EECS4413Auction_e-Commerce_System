package com.code2cash.auction.controller;

import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.dto.CreateAuctionRequest;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.service.AuctionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionService auctionService;

    private String tokenHeader;

    @BeforeEach
    void setUp() {
        tokenHeader = "Bearer token";
    }

    @Test
    @DisplayName("POST /api/auctions - valid request returns 201 and AuctionResponse JSON")
    void testCreateAuction_WithValidToken_ReturnsCreated() throws Exception {
        CreateAuctionRequest request = new CreateAuctionRequest();
        request.setItemId("ITEM_1");
        request.setSellerId("SELLER_FROM_BODY");
        request.setDurationHours(24);
        request.setReservePrice(BigDecimal.valueOf(99.99));

        AuctionResponse response = new AuctionResponse();
        response.setAuctionId("AUC_1");
        response.setItemId("ITEM_1");
        response.setSellerId("SELLER_1");
        response.setStatus(AuctionStatus.ACTIVE);
        response.setStartTime(LocalDateTime.now());
        response.setEndTime(LocalDateTime.now().plusHours(24));
        response.setReservePrice(BigDecimal.valueOf(99.99));
        response.setCurrentHighestBid(BigDecimal.ZERO);

        when(auctionService.createAuction(any(CreateAuctionRequest.class), eq("token"))).thenReturn(response);

        mockMvc.perform(
                        post("/api/auctions")
                                .header("Authorization", tokenHeader)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auctionId").value("AUC_1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(auctionService).createAuction(any(CreateAuctionRequest.class), eq("token"));
    }

    @Test
    @DisplayName("POST /api/auctions - invalid body returns 400")
    void testCreateAuction_InvalidBody_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"itemId\":\"ITEM_1\",\"sellerId\":\"SELLER_1\",\"durationHours\":-1,\"reservePrice\":0}";

        mockMvc.perform(
                        post("/api/auctions")
                                .header("Authorization", tokenHeader)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(auctionService);
    }

    @Test
    @DisplayName("POST /api/auctions/close-expired - returns closed count message")
    void testCloseExpiredAuctions_ReturnsClosedMessage() throws Exception {
        when(auctionService.closeExpiredAuctions()).thenReturn(3);

        mockMvc.perform(post("/api/auctions/close-expired"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Closed 3 expired auctions")));

        verify(auctionService).closeExpiredAuctions();
    }
}

