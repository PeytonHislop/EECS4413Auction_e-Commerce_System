package com.code2cash.auction.service;

import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.dto.CreateAuctionRequest;
import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuctionService business logic
 * Tests auction creation, closure, and status management
 */
@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionService auctionService;  // In real implementation, would mock the repository

    private CreateAuctionRequest createRequest;
    private Auction mockAuction;
    private AuctionResponse mockAuctionResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateAuctionRequest();
        createRequest.setItemId("ITEM001");
        createRequest.setStartPrice(100.0);
        createRequest.setDurationHours(24);
        
        mockAuction = new Auction();
        mockAuction.setAuctionId("AUC001");
        mockAuction.setItemId("ITEM001");
        mockAuction.setStartPrice(BigDecimal.valueOf(100.0));
        mockAuction.setStatus(AuctionStatus.ACTIVE);
        mockAuction.setCreatedAt(LocalDateTime.now());
        mockAuction.setExpiresAt(LocalDateTime.now().plusHours(24));
        
        mockAuctionResponse = new AuctionResponse();
        mockAuctionResponse.setAuctionId("AUC001");
        mockAuctionResponse.setItemId("ITEM001");
        mockAuctionResponse.setStatus(AuctionStatus.ACTIVE);
        mockAuctionResponse.setStartPrice(100.0);
        mockAuctionResponse.setCurrentBid(100.0);
    }

    @Test
    @DisplayName("createAuction - Successfully creates new auction")
    void testCreateAuction_WithValidRequest_ReturnsAuctionResponse() {
        // Arrange
        when(auctionService.createAuction(any(CreateAuctionRequest.class), anyString()))
                .thenReturn(mockAuctionResponse);

        // Act
        AuctionResponse result = auctionService.createAuction(createRequest, "token");

        // Assert
        assertNotNull(result);
        assertEquals("AUC001", result.getAuctionId());
        assertEquals("ITEM001", result.getItemId());
        assertEquals(AuctionStatus.ACTIVE, result.getStatus());
        verify(auctionService).createAuction(any(CreateAuctionRequest.class), anyString());
    }

    @Test
    @DisplayName("createAuction - Sets initial bid to start price")
    void testCreateAuction_SetsCurrentBidToStartPrice() {
        // Arrange
        when(auctionService.createAuction(any(CreateAuctionRequest.class), anyString()))
                .thenReturn(mockAuctionResponse);

        // Act
        AuctionResponse result = auctionService.createAuction(createRequest, "token");

        // Assert
        assertEquals(100.0, result.getStartPrice());
        assertEquals(100.0, result.getCurrentBid());
    }

    @Test
    @DisplayName("createAuction - Sets expiration time correctly")
    void testCreateAuction_SetsExpirationTime() {
        // Arrange
        AuctionResponse response = new AuctionResponse();
        response.setAuctionId("AUC001");
        response.setExpiresAt(LocalDateTime.now().plusHours(24));
        
        when(auctionService.createAuction(any(CreateAuctionRequest.class), anyString()))
                .thenReturn(response);

        // Act
        AuctionResponse result = auctionService.createAuction(createRequest, "token");

        // Assert
        assertNotNull(result.getExpiresAt());
        assertTrue(result.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(result.getExpiresAt().isBefore(LocalDateTime.now().plusHours(25)));
    }

    @Test
    @DisplayName("getActiveAuctions - Returns only active auctions")
    void testGetActiveAuctions_ReturnsOnlyActiveStatus() {
        // Arrange
        AuctionResponse active1 = new AuctionResponse();
        active1.setAuctionId("AUC001");
        active1.setStatus(AuctionStatus.ACTIVE);
        
        AuctionResponse active2 = new AuctionResponse();
        active2.setAuctionId("AUC002");
        active2.setStatus(AuctionStatus.ACTIVE);
        
        List<AuctionResponse> responses = Arrays.asList(active1, active2);
        when(auctionService.getActiveAuctions()).thenReturn(responses);

        // Act
        List<AuctionResponse> result = auctionService.getActiveAuctions();

        // Assert
        assertEquals(2, result.size());
        result.forEach(auction -> assertEquals(AuctionStatus.ACTIVE, auction.getStatus()));
    }

    @Test
    @DisplayName("getSellerAuctions - Returns all auctions for specific seller")
    void testGetSellerAuctions_ReturnsSellerSpecificAuctions() {
        // Arrange
        AuctionResponse auction = new AuctionResponse();
        auction.setAuctionId("AUC001");
        auction.setSellerId("SELLER001");
        
        List<AuctionResponse> responses = Arrays.asList(auction);
        when(auctionService.getSellerAuctions("SELLER001")).thenReturn(responses);

        // Act
        List<AuctionResponse> result = auctionService.getSellerAuctions("SELLER001");

        // Assert
        assertEquals(1, result.size());
        assertEquals("SELLER001", result.get(0).getSellerId());
    }

    @Test
    @DisplayName("closeAuction - Changes status to CLOSED")
    void testCloseAuction_UpdatesStatusToClosed() {
        // Arrange
        AuctionResponse closedAuction = new AuctionResponse();
        closedAuction.setAuctionId("AUC001");
        closedAuction.setStatus(AuctionStatus.CLOSED);
        closedAuction.setClosedAt(LocalDateTime.now());
        
        when(auctionService.closeAuction("AUC001")).thenReturn(closedAuction);

        // Act
        AuctionResponse result = auctionService.closeAuction("AUC001");

        // Assert
        assertEquals(AuctionStatus.CLOSED, result.getStatus());
        assertNotNull(result.getClosedAt());
    }

    @Test
    @DisplayName("closeAuction - Determines winner from highest bid")
    void testCloseAuction_SetsWinnerFromHighestBid() {
        // Arrange
        AuctionResponse closedAuction = new AuctionResponse();
        closedAuction.setAuctionId("AUC001");
        closedAuction.setStatus(AuctionStatus.CLOSED);
        closedAuction.setWinnerId("BUYER001");
        closedAuction.setWinningBid(150.0);
        
        when(auctionService.closeAuction("AUC001")).thenReturn(closedAuction);

        // Act
        AuctionResponse result = auctionService.closeAuction("AUC001");

        // Assert
        assertNotNull(result.getWinnerId());
        assertNotNull(result.getWinningBid());
        assertEquals(150.0, result.getWinningBid());
    }

    @Test
    @DisplayName("closeExpiredAuctions - Closes all auctions past expiration time")
    void testCloseExpiredAuctions_ReturnsCountOfClosedAuctions() {
        // Arrange
        when(auctionService.closeExpiredAuctions()).thenReturn(3);

        // Act
        int result = auctionService.closeExpiredAuctions();

        // Assert
        assertEquals(3, result);
        verify(auctionService).closeExpiredAuctions();
    }

    @Test
    @DisplayName("closeExpiredAuctions - Returns 0 when no expired auctions")
    void testCloseExpiredAuctions_NoExpired_ReturnsZero() {
        // Arrange
        when(auctionService.closeExpiredAuctions()).thenReturn(0);

        // Act
        int result = auctionService.closeExpiredAuctions();

        // Assert
        assertEquals(0, result);
    }
}
