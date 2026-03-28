package com.code2cash.auction.service;

import com.code2cash.auction.dto.BidResponse;
import com.code2cash.auction.model.Bid;
import com.code2cash.auction.model.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BidService business logic
 * Tests bid placement, validation, and history retrieval
 */
@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidService bidService;

    private Bid mockBid;
    private BidResponse mockBidResponse;

    @BeforeEach
    void setUp() {
        mockBid = new Bid();
        mockBid.setBidId("BID001");
        mockBid.setAuctionId("AUC001");
        mockBid.setBidAmount(BigDecimal.valueOf(150.0));
        mockBid.setBidderId("BUYER001");
        mockBid.setCreatedAt(LocalDateTime.now());
        
        mockBidResponse = new BidResponse();
        mockBidResponse.setBidId("BID001");
        mockBidResponse.setAuctionId("AUC001");
        mockBidResponse.setBidAmount(150.0);
        mockBidResponse.setBidderId("BUYER001");
        mockBidResponse.setSuccessful(true);
    }

    @Test
    @DisplayName("placeBid - Successfully places valid bid")
    void testPlaceBid_WithValidBid_ReturnsSuccessfulResponse() {
        // Arrange
        when(bidService.placeBid("AUC001", 150.0, "token"))
                .thenReturn(mockBidResponse);

        // Act
        BidResponse result = bidService.placeBid("AUC001", 150.0, "token");

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals("BID001", result.getBidId());
        assertEquals(150.0, result.getBidAmount());
        verify(bidService).placeBid("AUC001", 150.0, "token");
    }

    @Test
    @DisplayName("placeBid - Validates bid is higher than current")
    void testPlaceBid_LowerThanCurrent_ReturnsFailed() {
        // Arrange
        BidResponse failedBid = new BidResponse();
        failedBid.setSuccessful(false);
        failedBid.setMessage("Bid must be higher than current bid of 100.0");
        
        when(bidService.placeBid("AUC001", 50.0, "token"))
                .thenReturn(failedBid);

        // Act
        BidResponse result = bidService.placeBid("AUC001", 50.0, "token");

        // Assert
        assertFalse(result.isSuccessful());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("higher"));
    }

    @Test
    @DisplayName("placeBid - Fails on closed auction")
    void testPlaceBid_ClosedAuction_ReturnsFailed() {
        // Arrange
        BidResponse failedBid = new BidResponse();
        failedBid.setSuccessful(false);
        failedBid.setMessage("Auction is closed");
        
        when(bidService.placeBid("AUC001", 200.0, "token"))
                .thenReturn(failedBid);

        // Act
        BidResponse result = bidService.placeBid("AUC001", 200.0, "token");

        // Assert
        assertFalse(result.isSuccessful());
        assertEquals("Auction is closed", result.getMessage());
    }

    @Test
    @DisplayName("placeBid - Sets bidder from authenticated token")
    void testPlaceBid_SetsBidderFromToken() {
        // Arrange
        BidResponse response = new BidResponse();
        response.setSuccessful(true);
        response.setBidderId("BUYER001");
        
        when(bidService.placeBid(eq("AUC001"), anyDouble(), eq("token")))
                .thenReturn(response);

        // Act
        BidResponse result = bidService.placeBid("AUC001", 150.0, "token");

        // Assert
        assertNotNull(result.getBidderId());
        assertEquals("BUYER001", result.getBidderId());
    }

    @Test
    @DisplayName("getBidHistory - Returns bids in chronological order")
    void testGetBidHistory_ReturnsBidsInOrder() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidId("BID001");
        bid1.setBidAmount(BigDecimal.valueOf(100.0));
        bid1.setCreatedAt(LocalDateTime.now().minusHours(2));
        
        Bid bid2 = new Bid();
        bid2.setBidId("BID002");
        bid2.setBidAmount(BigDecimal.valueOf(150.0));
        bid2.setCreatedAt(LocalDateTime.now().minusHours(1));
        
        Bid bid3 = new Bid();
        bid3.setBidId("BID003");
        bid3.setBidAmount(BigDecimal.valueOf(200.0));
        bid3.setCreatedAt(LocalDateTime.now());
        
        List<Bid> bids = Arrays.asList(bid1, bid2, bid3);
        when(bidService.getBidHistory("AUC001")).thenReturn(bids);

        // Act
        List<Bid> result = bidService.getBidHistory("AUC001");

        // Assert
        assertEquals(3, result.size());
        assertEquals("BID001", result.get(0).getBidId());
        assertEquals("BID002", result.get(1).getBidId());
        assertEquals("BID003", result.get(2).getBidId());
        // Verify amounts are increasing
        assertTrue(result.get(0).getBidAmount().compareTo(result.get(1).getBidAmount()) < 0);
        assertTrue(result.get(1).getBidAmount().compareTo(result.get(2).getBidAmount()) < 0);
    }

    @Test
    @DisplayName("getBidHistory - Returns empty list for auction with no bids")
    void testGetBidHistory_NoAuction_ReturnsEmptyList() {
        // Arrange
        when(bidService.getBidHistory("AUC999")).thenReturn(Arrays.asList());

        // Act
        List<Bid> result = bidService.getBidHistory("AUC999");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bidService).getBidHistory("AUC999");
    }

    @Test
    @DisplayName("getHighestBid - Returns bid with maximum amount")
    void testGetHighestBid_ReturnsMaxBid() {
        // Arrange
        Bid highestBid = new Bid();
        highestBid.setBidId("BID003");
        highestBid.setBidAmount(BigDecimal.valueOf(200.0));
        highestBid.setBidderId("BUYER003");
        
        when(bidService.getHighestBid("AUC001")).thenReturn(highestBid);

        // Act
        Bid result = bidService.getHighestBid("AUC001");

        // Assert
        assertNotNull(result);
        assertEquals("BID003", result.getBidId());
        assertEquals(BigDecimal.valueOf(200.0), result.getBidAmount());
        assertEquals("BUYER003", result.getBidderId());
    }

    @Test
    @DisplayName("getHighestBid - Returns null for auction with no bids")
    void testGetHighestBid_NoBids_ReturnsNull() {
        // Arrange
        when(bidService.getHighestBid("AUC999")).thenReturn(null);

        // Act
        Bid result = bidService.getHighestBid("AUC999");

        // Assert
        assertNull(result);
        verify(bidService).getHighestBid("AUC999");
    }

    @Test
    @DisplayName("getHighestBid - Works correctly with multiple bids")
    void testGetHighestBid_MultipleBids_ReturnsMaximum() {
        // Arrange
        Bid maxBid = new Bid();
        maxBid.setBidAmount(BigDecimal.valueOf(500.0));
        
        when(bidService.getHighestBid("AUC001")).thenReturn(maxBid);

        // Act
        Bid result = bidService.getHighestBid("AUC001");

        // Assert
        assertEquals(BigDecimal.valueOf(500.0), result.getBidAmount());
    }

    @Test
    @DisplayName("getBidsByBidder - Returns all bids placed by specific bidder")
    void testGetBidsByBidder_ReturnsBidderSpecificBids() {
        // Arrange
        Bid bid1 = new Bid();
        bid1.setBidderId("BUYER001");
        bid1.setAuctionId("AUC001");
        
        Bid bid2 = new Bid();
        bid2.setBidderId("BUYER001");
        bid2.setAuctionId("AUC002");
        
        List<Bid> bids = Arrays.asList(bid1, bid2);
        when(bidService.getBidsByBidder("BUYER001")).thenReturn(bids);

        // Act
        List<Bid> result = bidService.getBidsByBidder("BUYER001");

        // Assert
        assertEquals(2, result.size());
        result.forEach(bid -> assertEquals("BUYER001", bid.getBidderId()));
    }
}
