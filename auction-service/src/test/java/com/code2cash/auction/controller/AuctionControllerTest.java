package com.code2cash.auction.controller;

import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.service.AuctionService;
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
 * Unit tests for AuctionController
 * Uses MockMvc to test REST endpoints without starting full server
 */
@WebMvcTest(AuctionController.class)
public class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @Test
    public void testGetActiveAuctions_ReturnsOk() throws Exception {
        // Arrange
        AuctionResponse auction1 = new AuctionResponse();
        auction1.setAuctionId("AUC001");
        auction1.setItemId("ITEM001");
        auction1.setStatus(AuctionStatus.ACTIVE);
        
        List<AuctionResponse> auctions = Arrays.asList(auction1);
        when(auctionService.getActiveAuctions()).thenReturn(auctions);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].auctionId").value("AUC001"));
    }

    @Test
    public void testGetAuctionById_ReturnsOk() throws Exception {
        // Arrange
        AuctionResponse auction = new AuctionResponse();
        auction.setAuctionId("AUC001");
        auction.setItemId("ITEM001");
        
        when(auctionService.getAuction("AUC001")).thenReturn(auction);

        // Act & Assert
        mockMvc.perform(get("/api/auctions/AUC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctionId").value("AUC001"));
    }
}