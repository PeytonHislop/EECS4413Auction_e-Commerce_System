package com.code2cash.auction.repository;

import com.code2cash.auction.model.Bid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BidRepository
 * Tests database queries for bid data access
 */
@DataJpaTest
class BidRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BidRepository bidRepository;

    private Bid bid1;
    private Bid bid2;
    private Bid bid3;

    @BeforeEach
    void setUp() {
        // Create bid by first bidder
        bid1 = new Bid();
        bid1.setAuctionId("AUC001");
        bid1.setBidderId("BUYER001");
        bid1.setBidAmount(BigDecimal.valueOf(100.0));
        bid1.setCreatedAt(LocalDateTime.now().minusHours(2));
        
        // Create second bid by different bidder (higher amount)
        bid2 = new Bid();
        bid2.setAuctionId("AUC001");
        bid2.setBidderId("BUYER002");
        bid2.setBidAmount(BigDecimal.valueOf(150.0));
        bid2.setCreatedAt(LocalDateTime.now().minusHours(1));
        
        // Create third bid (highest)
        bid3 = new Bid();
        bid3.setAuctionId("AUC001");
        bid3.setBidderId("BUYER003");
        bid3.setBidAmount(BigDecimal.valueOf(200.0));
        bid3.setCreatedAt(LocalDateTime.now());
        
        entityManager.persist(bid1);
        entityManager.persist(bid2);
        entityManager.persist(bid3);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByAuctionId - Returns all bids for auction")
    void testFindByAuctionId_ReturnsAllAuctionBids() {
        // Act
        List<Bid> result = bidRepository.findByAuctionId("AUC001");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        result.forEach(bid -> assertEquals("AUC001", bid.getAuctionId()));
    }

    @Test
    @DisplayName("findByAuctionId - Returns empty list for non-existent auction")
    void testFindByAuctionId_NoAuction_ReturnsEmptyList() {
        // Act
        List<Bid> result = bidRepository.findByAuctionId("NONEXISTENT");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("findByAuctionIdOrderByBidAmountDesc - Returns bids in descending order")
    void testFindByAuctionIdOrderByBidAmountDesc_ReturnsBidsInOrder() {
        // Act
        List<Bid> result = bidRepository.findByAuctionIdOrderByBidAmountDesc("AUC001");

        // Assert
        assertEquals(3, result.size());
        assertEquals(BigDecimal.valueOf(200.0), result.get(0).getBidAmount());
        assertEquals(BigDecimal.valueOf(150.0), result.get(1).getBidAmount());
        assertEquals(BigDecimal.valueOf(100.0), result.get(2).getBidAmount());
    }

    @Test
    @DisplayName("findByAuctionIdOrderByCreatedAtAsc - Returns bids chronologically")
    void testFindByAuctionIdOrderByCreatedAtAsc_ReturnsBidsInChronological() {
        // Act
        List<Bid> result = bidRepository.findByAuctionIdOrderByCreatedAtAsc("AUC001");

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.get(0).getCreatedAt().isBefore(result.get(1).getCreatedAt()));
        assertTrue(result.get(1).getCreatedAt().isBefore(result.get(2).getCreatedAt()));
    }

    @Test
    @DisplayName("findTopByAuctionIdOrderByBidAmountDesc - Returns highest bid")
    void testFindHighestBid_ReturnsMaximumBid() {
        // Act
        Bid result = bidRepository.findTopByAuctionIdOrderByBidAmountDesc("AUC001");

        // Assert
        assertNotNull(result);
        assertEquals("BUYER003", result.getBidderId());
        assertEquals(BigDecimal.valueOf(200.0), result.getBidAmount());
    }

    @Test
    @DisplayName("findTopByAuctionIdOrderByBidAmountDesc - No bids returns null")
    void testFindHighestBid_NoBids_ReturnsNull() {
        // Act
        Bid result = bidRepository.findTopByAuctionIdOrderByBidAmountDesc("NONEXISTENT");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("findByBidderId - Returns all bids by specific bidder")
    void testFindByBidderId_ReturnsBidderBids() {
        // Arrange - create bid for different auction by same bidder
        Bid bid4 = new Bid();
        bid4.setAuctionId("AUC002");
        bid4.setBidderId("BUYER001");
        bid4.setBidAmount(BigDecimal.valueOf(50.0));
        bid4.setCreatedAt(LocalDateTime.now());
        entityManager.persist(bid4);
        entityManager.flush();

        // Act
        List<Bid> result = bidRepository.findByBidderId("BUYER001");

        // Assert
        assertEquals(2, result.size());
        result.forEach(bid -> assertEquals("BUYER001", bid.getBidderId()));
    }

    @Test
    @DisplayName("findByBidderId - Empty list for bidder with no bids")
    void testFindByBidderId_NoBids_ReturnsEmptyList() {
        // Act
        List<Bid> result = bidRepository.findByBidderId("NONEXISTENT");

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("save - Persists new bid to database")
    void testSave_NewBid_PersistsToDatabase() {
        // Arrange
        Bid newBid = new Bid();
        newBid.setAuctionId("AUC001");
        newBid.setBidderId("BUYER004");
        newBid.setBidAmount(BigDecimal.valueOf(250.0));
        newBid.setCreatedAt(LocalDateTime.now());

        // Act
        Bid saved = bidRepository.save(newBid);

        // Assert
        assertNotNull(saved.getBidId());
        
        // Verify
        Optional<Bid> retrieved = bidRepository.findById(saved.getBidId());
        assertTrue(retrieved.isPresent());
        assertEquals("BUYER004", retrieved.get().getBidderId());
    }

    @Test
    @DisplayName("delete - Removes bid from database")
    void testDelete_ExistingBid_RemovesFromDatabase() {
        // Arrange
        String bidId = bid1.getBidId();

        // Act
        bidRepository.delete(bid1);

        // Assert
        Optional<Bid> result = bidRepository.findById(bidId);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("countByAuctionId - Returns bid count for auction")
    void testCountByAuctionId_ReturnsCorrectCount() {
        // Act
        long count = bidRepository.countByAuctionId("AUC001");

        // Assert
        assertEquals(3, count);
    }

    @Test
    @DisplayName("countByAuctionId - Returns 0 for non-existent auction")
    void testCountByAuctionId_NoAuction_ReturnsZero() {
        // Act
        long count = bidRepository.countByAuctionId("NONEXISTENT");

        // Assert
        assertEquals(0, count);
    }

    @Test
    @DisplayName("findByAuctionId with Pageable - Supports pagination")
    void testFindByAuctionId_WithPagination_ReturnsPagedBids() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        List<Bid> result = bidRepository.findByAuctionId("AUC001", pageable).getContent();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByAuctionIdAndBidAmountGreaterThan - Filters bids by amount")
    void testFindByAuctionIdAndBidAmountGreaterThan_ReturnsBidsAboveThreshold() {
        // Act
        List<Bid> result = bidRepository.findByAuctionIdAndBidAmountGreaterThan(
                "AUC001", 
                BigDecimal.valueOf(125.0)
        );

        // Assert
        assertEquals(2, result.size());
        result.forEach(bid -> assertTrue(
                bid.getBidAmount().compareTo(BigDecimal.valueOf(125.0)) > 0
        ));
    }

    @Test
    @DisplayName("findById - Returns optional with bid when exists")
    void testFindById_ExistingBid_ReturnsBid() {
        // Act
        Optional<Bid> result = bidRepository.findById(bid1.getBidId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("BUYER001", result.get().getBidderId());
    }

    @Test
    @DisplayName("findById - Returns empty optional when not found")
    void testFindById_NonExistent_ReturnsEmpty() {
        // Act
        Optional<Bid> result = bidRepository.findById("NONEXISTENT");

        // Assert
        assertTrue(result.isEmpty());
    }
}
