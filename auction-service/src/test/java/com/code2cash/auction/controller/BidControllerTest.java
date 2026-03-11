package com.code2cash.auction.controller;

import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.service.BidService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for BidController
 * Tests bid-related REST endpoints
 */
@WebMvcTest(BidController.class)
public class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BidService bidService;

    @Test
    public void testGetBidHistory_ReturnsOk() throws Exception {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("BID001");
        bid1.setAuctionId("AUC001");
        bid1.setBidAmount(BigDecimal.valueOf(100.00));
        
        List<Bid> bids = Arrays.asList(bid1);
        when(bidService.getBidHistory("AUC001")).thenReturn(bids);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC001/bids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bidId").value("BID001"));
    }

    @Test
    public void testGetHighestBid_ReturnsOk() throws Exception {
        // Arrange
        Bid highestBid = new Bid();
        highestBid.setBidId("BID001");
        highestBid.setBidAmount(BigDecimal.valueOf(150.00));
        
        when(bidService.getHighestBid("AUC001")).thenReturn(highestBid);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC001/highest-bid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidAmount").value(150.00));
    }
}
