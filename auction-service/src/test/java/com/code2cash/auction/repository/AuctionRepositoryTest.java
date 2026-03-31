package com.code2cash.auction.repository;

import com.code2cash.auction.AuctionServiceApplication;
import com.code2cash.auction.dao.AuctionDAO;
import com.code2cash.auction.dao.BidDAO;
import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.model.Bid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link AuctionDAO}.
 * Uses the real SQLite schema (schema.sql) and verifies JDBC query behavior.
 */
@SpringBootTest(classes = AuctionServiceApplication.class)
class AuctionRepositoryTest {

    private static final String DB_FILE_PATH = "target/auction-service-test-" + UUID.randomUUID() + ".db";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Ensure forward slashes for JDBC URL on Windows.
        String normalizedPath = DB_FILE_PATH.replace('\\', '/');
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + normalizedPath);
        registry.add("spring.datasource.driver-class-name", () -> "org.sqlite.JDBC");

        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");

        // Avoid scheduler side-effects during tests.
        registry.add("spring.task.scheduling.enabled", () -> "false");
    }

    @Autowired
    private AuctionDAO auctionDAO;

    @Autowired
    private BidDAO bidDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        // Keep this order because bids reference auctions.
        jdbcTemplate.update("DELETE FROM bids");
        jdbcTemplate.update("DELETE FROM auctions");
    }

    private Auction createAuction(String auctionId, AuctionStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setItemId("ITEM_" + auctionId);
        auction.setSellerId("SELLER_" + auctionId);
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
        auction.setStatus(status);
        auction.setReservePrice(BigDecimal.valueOf(10.00));

        // Default current/highest values.
        auction.setCurrentHighestBid(BigDecimal.ZERO);
        auction.setCurrentHighestBidderId(null);
        auction.setWinnerId(null);

        return auctionDAO.createAuction(auction);
    }

    @Test
    @DisplayName("createAuction + findById - round-trip key auction fields")
    void testCreateAuctionAndFindById_RoundTrip() {
        String auctionId = "AUC_TEST_CREATE";
        LocalDateTime start = LocalDateTime.of(2026, 3, 5, 8, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 5, 10, 0, 0);

        Auction created = createAuction(auctionId, AuctionStatus.ACTIVE, start, end);
        assertEquals(auctionId, created.getAuctionId());

        Optional<Auction> found = auctionDAO.findById(auctionId);
        assertTrue(found.isPresent());

        Auction auction = found.get();
        assertEquals(auctionId, auction.getAuctionId());
        assertEquals("ITEM_" + auctionId, auction.getItemId());
        assertEquals("SELLER_" + auctionId, auction.getSellerId());
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
        assertEquals(start, auction.getStartTime());
        assertEquals(end, auction.getEndTime());
        assertNotNull(auction.getCreatedAt());
        assertNotNull(auction.getUpdatedAt());
    }

    @Test
    @DisplayName("findByStatus - returns only auctions matching status")
    void testFindByStatus_ReturnsOnlyAuctionsWithStatus() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime endFuture = LocalDateTime.now().plusHours(2);

        createAuction("AUC_STATUS_ACTIVE", AuctionStatus.ACTIVE, start, endFuture);
        createAuction("AUC_STATUS_CLOSED", AuctionStatus.CLOSED, start, endFuture);

        List<Auction> active = auctionDAO.findByStatus(AuctionStatus.ACTIVE);
        assertNotNull(active);
        assertEquals(1, active.size());
        assertEquals("AUC_STATUS_ACTIVE", active.get(0).getAuctionId());
        assertEquals(AuctionStatus.ACTIVE, active.get(0).getStatus());

        List<Auction> closed = auctionDAO.findByStatus(AuctionStatus.CLOSED);
        assertNotNull(closed);
        assertEquals(1, closed.size());
        assertEquals("AUC_STATUS_CLOSED", closed.get(0).getAuctionId());
        assertEquals(AuctionStatus.CLOSED, closed.get(0).getStatus());
    }

    @Test
    @DisplayName("findActiveAuctions - returns ACTIVE auctions that have not expired")
    void testFindActiveAuctions_ReturnsOnlyActiveAndNotExpired() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 30, 0, 0, 0);

        // Should be returned
        LocalDateTime endActiveFuture = LocalDateTime.of(2099, 1, 1, 0, 0, 0);
        createAuction("AUC_ACTIVE_FUTURE", AuctionStatus.ACTIVE, start, endActiveFuture);

        // Should not be returned (expired)
        LocalDateTime endActivePast = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        createAuction("AUC_ACTIVE_EXPIRED", AuctionStatus.ACTIVE, start, endActivePast);

        // Should not be returned (wrong status)
        createAuction("AUC_CLOSED", AuctionStatus.CLOSED, start, endActiveFuture);

        List<Auction> result = auctionDAO.findActiveAuctions();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AUC_ACTIVE_FUTURE", result.get(0).getAuctionId());
        assertEquals(AuctionStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    @DisplayName("findBySellerId - returns auctions for seller")
    void testFindBySellerId_ReturnsSellerAuctions() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime endFuture = LocalDateTime.now().plusHours(2);

        createAuction("AUC_SELLER_1", AuctionStatus.ACTIVE, start, endFuture);
        createAuction("AUC_SELLER_2", AuctionStatus.ACTIVE, start, endFuture);

        // Override sellerId to make sure findBySellerId filters correctly.
        Auction auction = auctionDAO.findById("AUC_SELLER_2").orElseThrow();
        auction.setSellerId("SELLER_SHARED");
        auction.setItemId("ITEM_" + "AUC_SELLER_2_RENAMED");
        auctionDAO.updateAuction(auction);

        Auction auction1 = auctionDAO.findById("AUC_SELLER_1").orElseThrow();
        auction1.setSellerId("SELLER_SHARED");
        auctionDAO.updateAuction(auction1);

        List<Auction> result = auctionDAO.findBySellerId("SELLER_SHARED");
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(a -> assertEquals("SELLER_SHARED", a.getSellerId()));
    }

    @Test
    @DisplayName("updateHighestBid - updates highest bid amount and bidder id")
    void testUpdateHighestBid_UpdatesHighestBidAndBidder() {
        String auctionId = "AUC_TEST_UPDATE_HIGHEST";
        LocalDateTime start = LocalDateTime.of(2026, 3, 30, 0, 0, 0);
        LocalDateTime endFuture = LocalDateTime.of(2099, 1, 1, 0, 0, 0);
        createAuction(auctionId, AuctionStatus.ACTIVE, start, endFuture);

        boolean updated = auctionDAO.updateHighestBid(auctionId, BigDecimal.valueOf(200.00), "BUYER_WINNER");
        assertTrue(updated);

        Auction auction = auctionDAO.findById(auctionId).orElseThrow();
        assertEquals(0, auction.getCurrentHighestBid().compareTo(BigDecimal.valueOf(200.00)));
        assertEquals("BUYER_WINNER", auction.getCurrentHighestBidderId());
    }

    @Test
    @DisplayName("closeAuction - updates auction status and winner id")
    void testCloseAuction_UpdatesStatusAndWinnerId() {
        String auctionId = "AUC_TEST_CLOSE";
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime endFuture = LocalDateTime.now().plusHours(2);
        createAuction(auctionId, AuctionStatus.ACTIVE, start, endFuture);

        boolean closed = auctionDAO.closeAuction(auctionId, "WINNER_1", AuctionStatus.NO_SALE);
        assertTrue(closed);

        Auction auction = auctionDAO.findById(auctionId).orElseThrow();
        assertEquals(AuctionStatus.NO_SALE, auction.getStatus());
        assertEquals("WINNER_1", auction.getWinnerId());
    }

    @Test
    @DisplayName("findExpiredActiveAuctions - returns ACTIVE auctions whose end time has passed")
    void testFindExpiredActiveAuctions_ReturnsExpiredActiveAuctions() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 30, 0, 0, 0);

        createAuction("AUC_EXPIRED_1", AuctionStatus.ACTIVE, start, LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        createAuction("AUC_ACTIVE_FUTURE", AuctionStatus.ACTIVE, start, LocalDateTime.of(2099, 1, 1, 0, 0, 0));

        List<Auction> result = auctionDAO.findExpiredActiveAuctions();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AUC_EXPIRED_1", result.get(0).getAuctionId());
    }

    @Test
    @DisplayName("updateAuction - updates auction record fields")
    void testUpdateAuction_UpdatesAuctionFields() {
        String auctionId = "AUC_TEST_UPDATE";
        LocalDateTime start1 = LocalDateTime.of(2026, 3, 6, 9, 0, 0);
        LocalDateTime end1 = LocalDateTime.of(2026, 3, 6, 11, 0, 0);

        createAuction(auctionId, AuctionStatus.ACTIVE, start1, end1);

        Auction toUpdate = auctionDAO.findById(auctionId).orElseThrow();
        toUpdate.setItemId("ITEM_UPDATED");
        toUpdate.setSellerId("SELLER_UPDATED");
        toUpdate.setStartTime(LocalDateTime.of(2026, 3, 6, 10, 0, 0));
        toUpdate.setEndTime(LocalDateTime.of(2026, 3, 6, 12, 0, 0));
        toUpdate.setStatus(AuctionStatus.CLOSED);
        toUpdate.setReservePrice(BigDecimal.valueOf(999.99));
        toUpdate.setCurrentHighestBid(BigDecimal.valueOf(123.45));
        toUpdate.setCurrentHighestBidderId("BUYER_UPDATED");
        toUpdate.setWinnerId("BUYER_UPDATED");

        boolean updated = auctionDAO.updateAuction(toUpdate);
        assertTrue(updated);

        Auction updatedAuction = auctionDAO.findById(auctionId).orElseThrow();
        assertEquals("ITEM_UPDATED", updatedAuction.getItemId());
        assertEquals("SELLER_UPDATED", updatedAuction.getSellerId());
        assertEquals(AuctionStatus.CLOSED, updatedAuction.getStatus());
        assertEquals(BigDecimal.valueOf(999.99), updatedAuction.getReservePrice());
        assertEquals(BigDecimal.valueOf(123.45), updatedAuction.getCurrentHighestBid());
        assertEquals("BUYER_UPDATED", updatedAuction.getCurrentHighestBidderId());
        assertEquals("BUYER_UPDATED", updatedAuction.getWinnerId());
    }

    @Test
    @DisplayName("deleteAuction - deletes auction (and bids via ON DELETE CASCADE)")
    void testDeleteAuction_CascadesToBids() {
        String auctionId = "AUC_TEST_DELETE_CASCADE";
        LocalDateTime start = LocalDateTime.of(2026, 3, 30, 0, 0, 0);
        LocalDateTime endFuture = LocalDateTime.of(2099, 1, 1, 0, 0, 0);
        createAuction(auctionId, AuctionStatus.ACTIVE, start, endFuture);

        Bid b1 = new Bid();
        b1.setBidId("BID_C1");
        b1.setAuctionId(auctionId);
        b1.setBidderId("BUYER_1");
        b1.setBidAmount(BigDecimal.valueOf(10.00));
        b1.setBidTimestamp(LocalDateTime.now().minusMinutes(10));
        bidDAO.createBid(b1);

        Bid b2 = new Bid();
        b2.setBidId("BID_C2");
        b2.setAuctionId(auctionId);
        b2.setBidderId("BUYER_2");
        b2.setBidAmount(BigDecimal.valueOf(20.00));
        b2.setBidTimestamp(LocalDateTime.now().minusMinutes(5));
        bidDAO.createBid(b2);

        assertEquals(2, bidDAO.findByAuctionId(auctionId).size());

        auctionDAO.deleteAuction(auctionId);

        assertTrue(auctionDAO.findById(auctionId).isEmpty());
        assertTrue(bidDAO.findByAuctionId(auctionId).isEmpty());
    }

    @Test
    @DisplayName("findById - returns empty optional for unknown auction id")
    void testFindById_UnknownAuction_ReturnsEmptyOptional() {
        Optional<Auction> result = auctionDAO.findById("DOES_NOT_EXIST");
        assertTrue(result.isEmpty());
    }
}

