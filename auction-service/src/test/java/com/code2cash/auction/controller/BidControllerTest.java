package com.code2cash.auction.controller;

import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.dto.PlaceBidRequest;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.service.BidService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BidController.class)
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BidService bidService;

    private String tokenHeader;
    private String auctionId;

    @BeforeEach
    void setUp() {
        tokenHeader = "Bearer token";
        auctionId = "AUC_1";
    }

    @Test
    @DisplayName("POST /api/auctions/{auctionId}/bids - success returns BidResponse JSON")
    void testPlaceBid_WithValidTokenAndBody_ReturnsOk() throws Exception {
        PlaceBidRequest request = new PlaceBidRequest();
        request.setBidderId("BUYER_1");
        request.setBidAmount(BigDecimal.valueOf(150.00));

        BidResponse response = new BidResponse(true, "Bid placed successfully");
        response.setBidId("BID_1");
        response.setAuctionId(auctionId);
        response.setBidderId("BUYER_1");
        response.setBidAmount(BigDecimal.valueOf(150.00));
        response.setBidTimestamp(LocalDateTime.of(2026, 3, 30, 10, 0));

        when(bidService.placeBid(eq(auctionId), any(PlaceBidRequest.class), eq("token"))).thenReturn(response);

        mockMvc.perform(
                        post("/api/auctions/{auctionId}/bids", auctionId)
                                .header("Authorization", tokenHeader)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidId").value("BID_1"))
                .andExpect(jsonPath("$.auctionId").value(auctionId))
                .andExpect(jsonPath("$.bidderId").value("BUYER_1"))
                .andExpect(jsonPath("$.success").value(true));

        verify(bidService).placeBid(eq(auctionId), any(PlaceBidRequest.class), eq("token"));
    }

    @Test
    @DisplayName("POST /api/auctions/{auctionId}/bids - invalid request body returns 400")
    void testPlaceBid_InvalidBody_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"bidderId\":\"\",\"bidAmount\":-1}";

        mockMvc.perform(
                        post("/api/auctions/{auctionId}/bids", auctionId)
                                .header("Authorization", tokenHeader)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bidService);
    }

    @Test
    @DisplayName("GET /api/auctions/{auctionId}/bids - returns bid history list")
    void testGetBidHistory_ReturnsBids() throws Exception {
        Bid bid1 = new Bid();
        bid1.setBidId("BID_1");
        bid1.setAuctionId(auctionId);
        bid1.setBidderId("BUYER_1");
        bid1.setBidAmount(BigDecimal.valueOf(100.00));
        bid1.setBidTimestamp(LocalDateTime.of(2026, 3, 30, 10, 0));

        Bid bid2 = new Bid();
        bid2.setBidId("BID_2");
        bid2.setAuctionId(auctionId);
        bid2.setBidderId("BUYER_2");
        bid2.setBidAmount(BigDecimal.valueOf(200.00));
        bid2.setBidTimestamp(LocalDateTime.of(2026, 3, 30, 10, 5));

        when(bidService.getBidHistory(auctionId)).thenReturn(List.of(bid1, bid2));

        mockMvc.perform(get("/api/auctions/{auctionId}/bids", auctionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].bidId").value("BID_1"))
                .andExpect(jsonPath("$[1].bidId").value("BID_2"));

        verify(bidService).getBidHistory(auctionId);
    }

    @Test
    @DisplayName("GET /api/auctions/{auctionId}/highest-bid - returns 204 when no bids")
    void testGetHighestBid_NoBids_ReturnsNoContent() throws Exception {
        when(bidService.getHighestBid(auctionId)).thenReturn(null);

        mockMvc.perform(get("/api/auctions/{auctionId}/highest-bid", auctionId))
                .andExpect(status().isNoContent());

        verify(bidService).getHighestBid(auctionId);
    }
}

