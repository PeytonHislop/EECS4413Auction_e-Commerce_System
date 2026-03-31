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
 * Integration tests for {@link BidDAO}.
 * Uses the real SQLite schema (schema.sql) and verifies JDBC query behavior.
 */
@SpringBootTest(classes = AuctionServiceApplication.class)
class BidRepositoryTest {

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
    private BidDAO bidDAO;

    @Autowired
    private AuctionDAO auctionDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        // Keep this order because bids reference auctions.
        jdbcTemplate.update("DELETE FROM bids");
        jdbcTemplate.update("DELETE FROM auctions");
    }

    private Auction createAuction(String auctionId) {
        Auction auction = new Auction();
        auction.setAuctionId(auctionId);
        auction.setItemId("ITEM_" + auctionId);
        auction.setSellerId("SELLER_" + auctionId);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setReservePrice(BigDecimal.valueOf(10.00));
        auction.setCurrentHighestBid(BigDecimal.ZERO);
        auction.setCurrentHighestBidderId(null);
        auction.setWinnerId(null);
        return auctionDAO.createAuction(auction);
    }

    private Bid createBid(String bidId, String auctionId, String bidderId, BigDecimal bidAmount, LocalDateTime bidTimestamp) {
        Bid bid = new Bid();
        bid.setBidId(bidId);
        bid.setAuctionId(auctionId);
        bid.setBidderId(bidderId);
        bid.setBidAmount(bidAmount);
        bid.setBidTimestamp(bidTimestamp);
        return bidDAO.createBid(bid);
    }

    @Test
    @DisplayName("findByAuctionId - returns bids ordered by newest timestamp")
    void testFindByAuctionId_ReturnsBidsInDescendingTimestampOrder() {
        String auctionId = "AUC_TEST_BIDS";
        createAuction(auctionId);

        LocalDateTime t1 = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 1, 10, 5, 0);
        LocalDateTime t3 = LocalDateTime.of(2026, 3, 1, 10, 10, 0);

        createBid("BID_1", auctionId, "BUYER_1", BigDecimal.valueOf(100.00), t1);
        createBid("BID_2", auctionId, "BUYER_2", BigDecimal.valueOf(200.00), t3); // newest
        createBid("BID_3", auctionId, "BUYER_3", BigDecimal.valueOf(150.00), t2);

        List<Bid> result = bidDAO.findByAuctionId(auctionId);
        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals("BID_2", result.get(0).getBidId());
        assertEquals("BID_3", result.get(1).getBidId());
        assertEquals("BID_1", result.get(2).getBidId());
    }

    @Test
    @DisplayName("findByAuctionId - empty list for auction with no bids")
    void testFindByAuctionId_NoBids_ReturnsEmptyList() {
        String auctionId = "AUC_TEST_EMPTY";
        createAuction(auctionId);

        List<Bid> result = bidDAO.findByAuctionId(auctionId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByAuctionId - empty list for unknown auction")
    void testFindByAuctionId_UnknownAuction_ReturnsEmptyList() {
        List<Bid> result = bidDAO.findByAuctionId("DOES_NOT_EXIST");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByBidderId - returns only bids for bidder ordered by newest timestamp")
    void testFindByBidderId_ReturnsBidsForBidderInDescendingTimestampOrder() {
        String auctionId1 = "AUC_TEST_B1";
        String auctionId2 = "AUC_TEST_B2";
        createAuction(auctionId1);
        createAuction(auctionId2);

        LocalDateTime t1 = LocalDateTime.of(2026, 3, 2, 9, 0, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 2, 9, 30, 0);

        createBid("BID_B1_OLD", auctionId1, "BUYER_X", BigDecimal.valueOf(10.00), t1);
        createBid("BID_B2_NEW", auctionId2, "BUYER_X", BigDecimal.valueOf(20.00), t2); // newest for BUYER_X
        createBid("BID_OTHER", auctionId2, "BUYER_Y", BigDecimal.valueOf(30.00), t2);

        List<Bid> result = bidDAO.findByBidderId("BUYER_X");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("BID_B2_NEW", result.get(0).getBidId());
        assertEquals("BID_B1_OLD", result.get(1).getBidId());
        result.forEach(b -> assertEquals("BUYER_X", b.getBidderId()));
    }

    @Test
    @DisplayName("findByBidderId - empty list for bidder with no bids")
    void testFindByBidderId_NoBids_ReturnsEmptyList() {
        createAuction("AUC_TEST_BIDDER_EMPTY");

        List<Bid> result = bidDAO.findByBidderId("NO_SUCH_BUYER");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findHighestBidByAuctionId - picks highest amount, then newest timestamp for ties")
    void testFindHighestBidByAuctionId_ReturnsHighestAmountThenNewest() {
        String auctionId = "AUC_TEST_HIGHEST";
        createAuction(auctionId);

        // Two bids with the same highest amount (200) - newest should win.
        LocalDateTime t1 = LocalDateTime.of(2026, 3, 3, 12, 0, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 3, 3, 12, 10, 0);

        createBid("BID_LOW", auctionId, "BUYER_1", BigDecimal.valueOf(150.00), t1);
        createBid("BID_HIGHEST_OLD", auctionId, "BUYER_2", BigDecimal.valueOf(200.00), t1);
        createBid("BID_HIGHEST_NEW", auctionId, "BUYER_3", BigDecimal.valueOf(200.00), t2);

        Optional<Bid> result = bidDAO.findHighestBidByAuctionId(auctionId);
        assertTrue(result.isPresent());

        Bid highest = result.get();
        assertEquals("BUYER_3", highest.getBidderId());
        assertEquals(0, highest.getBidAmount().compareTo(BigDecimal.valueOf(200.00)));
    }

    @Test
    @DisplayName("findHighestBidByAuctionId - empty optional for auctions with no bids")
    void testFindHighestBidByAuctionId_NoBids_ReturnsEmptyOptional() {
        String auctionId = "AUC_TEST_HIGHEST_EMPTY";
        createAuction(auctionId);

        Optional<Bid> result = bidDAO.findHighestBidByAuctionId(auctionId);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createBid + findById - persists and retrieves key bid fields")
    void testCreateBidAndFindById_RoundTrip() {
        String auctionId = "AUC_TEST_ROUNDTRIP";
        createAuction(auctionId);

        LocalDateTime ts = LocalDateTime.of(2026, 3, 4, 13, 0, 0);
        Bid created = createBid("BID_RT", auctionId, "BUYER_RT", BigDecimal.valueOf(42.50), ts);

        Optional<Bid> found = bidDAO.findById(created.getBidId());
        assertTrue(found.isPresent());

        Bid bid = found.get();
        assertEquals(auctionId, bid.getAuctionId());
        assertEquals("BUYER_RT", bid.getBidderId());
        assertEquals(BigDecimal.valueOf(42.50), bid.getBidAmount());
        assertEquals("BID_RT", bid.getBidId());
    }

    @Test
    @DisplayName("findById - empty optional for unknown bid id")
    void testFindById_UnknownBid_ReturnsEmptyOptional() {
        Optional<Bid> result = bidDAO.findById("DOES_NOT_EXIST");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("deleteBidsByAuctionId - removes bids for a single auction")
    void testDeleteBidsByAuctionId_RemovesOnlyTargetAuctionBids() {
        String auctionId1 = "AUC_TEST_DELETE_1";
        String auctionId2 = "AUC_TEST_DELETE_2";
        createAuction(auctionId1);
        createAuction(auctionId2);

        createBid("BID_D1", auctionId1, "BUYER_1", BigDecimal.valueOf(10.00), LocalDateTime.now().minusMinutes(10));
        createBid("BID_D2", auctionId1, "BUYER_1", BigDecimal.valueOf(20.00), LocalDateTime.now().minusMinutes(5));
        createBid("BID_OTHER", auctionId2, "BUYER_2", BigDecimal.valueOf(30.00), LocalDateTime.now().minusMinutes(1));

        int deleted = bidDAO.deleteBidsByAuctionId(auctionId1);
        assertTrue(deleted >= 1);

        assertTrue(bidDAO.findByAuctionId(auctionId1).isEmpty());
        assertEquals(1, bidDAO.findByAuctionId(auctionId2).size());
    }

    @Test
    @DisplayName("getBidCount - returns correct number of bids for an auction")
    void testGetBidCount_ReturnsCorrectCount() {
        String auctionId = "AUC_TEST_COUNT";
        createAuction(auctionId);

        createBid("BID_C1", auctionId, "BUYER_1", BigDecimal.valueOf(1.00), LocalDateTime.now().minusMinutes(10));
        createBid("BID_C2", auctionId, "BUYER_2", BigDecimal.valueOf(2.00), LocalDateTime.now().minusMinutes(5));
        createBid("BID_C3", auctionId, "BUYER_3", BigDecimal.valueOf(3.00), LocalDateTime.now().minusMinutes(1));

        assertEquals(3, bidDAO.getBidCount(auctionId));
        assertEquals(0, bidDAO.getBidCount("DOES_NOT_EXIST"));
    }
}

