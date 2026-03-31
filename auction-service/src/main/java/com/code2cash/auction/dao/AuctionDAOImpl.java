package com.code2cash.auction.dao;

import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.AuctionStatus;
import com.code2cash.auction.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AuctionDAO using Spring JdbcTemplate
 * Handles all database operations for auctions using SQLite
 */
@Repository
public class AuctionDAOImpl implements AuctionDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // RowMapper to convert ResultSet to Auction object
    private final RowMapper<Auction> auctionRowMapper = new RowMapper<Auction>() {
        @Override
        public Auction mapRow(ResultSet rs, int rowNum) throws SQLException {
            Auction auction = new Auction();
            auction.setAuctionId(rs.getString("auction_id"));
            auction.setItemId(rs.getString("item_id"));
            auction.setSellerId(rs.getString("seller_id"));
            
            // Convert SQL Timestamp to LocalDateTime
            Timestamp startTs = rs.getTimestamp("start_time");
            Timestamp endTs = rs.getTimestamp("end_time");
            Timestamp createdTs = rs.getTimestamp("created_at");
            Timestamp updatedTs = rs.getTimestamp("updated_at");
            
            if (startTs != null) auction.setStartTime(startTs.toLocalDateTime());
            if (endTs != null) auction.setEndTime(endTs.toLocalDateTime());
            if (createdTs != null) auction.setCreatedAt(createdTs.toLocalDateTime());
            if (updatedTs != null) auction.setUpdatedAt(updatedTs.toLocalDateTime());
            
            // Status
            String statusStr = rs.getString("status");
            auction.setStatus(AuctionStatus.valueOf(statusStr));
            
            // Prices
            BigDecimal reservePrice = rs.getBigDecimal("reserve_price");
            BigDecimal currentHighestBid = rs.getBigDecimal("current_highest_bid");
            if (reservePrice != null) auction.setReservePrice(reservePrice);
            if (currentHighestBid != null) auction.setCurrentHighestBid(currentHighestBid);
            
            // IDs (can be null)
            auction.setCurrentHighestBidderId(rs.getString("current_highest_bidder_id"));
            auction.setWinnerId(rs.getString("winner_id"));
            
            return auction;
        }
    };
    
    @Override
    public Auction createAuction(Auction auction) {
        // Generate ID if not present
        if (auction.getAuctionId() == null || auction.getAuctionId().isEmpty()) {
            auction.setAuctionId(IdGenerator.generateAuctionId());
        }
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        auction.setCreatedAt(now);
        auction.setUpdatedAt(now);
        
        // Initialize current highest bid to 0 if null
        if (auction.getCurrentHighestBid() == null) {
            auction.setCurrentHighestBid(BigDecimal.ZERO);
        }
        
        String sql = "INSERT INTO auctions (auction_id, item_id, seller_id, start_time, end_time, " +
                     "status, reserve_price, current_highest_bid, current_highest_bidder_id, " +
                     "winner_id, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            auction.getAuctionId(),
            auction.getItemId(),
            auction.getSellerId(),
            Timestamp.valueOf(auction.getStartTime()),
            Timestamp.valueOf(auction.getEndTime()),
            auction.getStatus().name(),
            auction.getReservePrice(),
            auction.getCurrentHighestBid(),
            auction.getCurrentHighestBidderId(),
            auction.getWinnerId(),
            Timestamp.valueOf(auction.getCreatedAt()),
            Timestamp.valueOf(auction.getUpdatedAt())
        );
        
        return auction;
    }
    
    @Override
    public Optional<Auction> findById(String auctionId) {
        String sql = "SELECT * FROM auctions WHERE auction_id = ?";
        try {
            Auction auction = jdbcTemplate.queryForObject(sql, auctionRowMapper, auctionId);
            return Optional.ofNullable(auction);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Auction> findActiveAuctions() {
        // Avoid SQLite TIMESTAMP parsing quirks: load ACTIVE auctions and filter by end_time in Java.
        String sql = "SELECT * FROM auctions WHERE status = 'ACTIVE' ORDER BY end_time ASC";
        List<Auction> auctions = jdbcTemplate.query(sql, auctionRowMapper);

        LocalDateTime now = LocalDateTime.now();
        return auctions.stream()
                .filter(a -> a.getEndTime() != null && a.getEndTime().isAfter(now))
                .toList();
    }
    
    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
        String sql = "SELECT * FROM auctions WHERE status = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, auctionRowMapper, status.name());
    }
    
    @Override
    public List<Auction> findBySellerId(String sellerId) {
        String sql = "SELECT * FROM auctions WHERE seller_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, auctionRowMapper, sellerId);
    }
    
    @Override
    public boolean updateAuction(Auction auction) {
        auction.setUpdatedAt(LocalDateTime.now());
        
        String sql = "UPDATE auctions SET item_id = ?, seller_id = ?, start_time = ?, end_time = ?, " +
                     "status = ?, reserve_price = ?, current_highest_bid = ?, " +
                     "current_highest_bidder_id = ?, winner_id = ?, updated_at = ? " +
                     "WHERE auction_id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            auction.getItemId(),
            auction.getSellerId(),
            Timestamp.valueOf(auction.getStartTime()),
            Timestamp.valueOf(auction.getEndTime()),
            auction.getStatus().name(),
            auction.getReservePrice(),
            auction.getCurrentHighestBid(),
            auction.getCurrentHighestBidderId(),
            auction.getWinnerId(),
            Timestamp.valueOf(auction.getUpdatedAt()),
            auction.getAuctionId()
        );
        
        return rowsAffected > 0;
    }
    
    @Override
    public boolean updateHighestBid(String auctionId, BigDecimal bidAmount, String bidderId) {
        String sql = "UPDATE auctions SET current_highest_bid = ?, current_highest_bidder_id = ?, " +
                     "updated_at = datetime('now') WHERE auction_id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql, bidAmount, bidderId, auctionId);
        return rowsAffected > 0;
    }
    
    @Override
    public boolean closeAuction(String auctionId, String winnerId, AuctionStatus status) {
        String sql = "UPDATE auctions SET status = ?, winner_id = ?, updated_at = datetime('now') " +
                     "WHERE auction_id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql, status.name(), winnerId, auctionId);
        return rowsAffected > 0;
    }
    
    @Override
    public List<Auction> findExpiredActiveAuctions() {
        // Avoid SQLite TIMESTAMP parsing quirks: load ACTIVE auctions and filter by end_time in Java.
        String sql = "SELECT * FROM auctions WHERE status = 'ACTIVE'";
        List<Auction> auctions = jdbcTemplate.query(sql, auctionRowMapper);

        LocalDateTime now = LocalDateTime.now();
        return auctions.stream()
                .filter(a -> a.getEndTime() != null && !a.getEndTime().isAfter(now))
                .toList();
    }
    
    @Override
    public boolean deleteAuction(String auctionId) {
        String sql = "DELETE FROM auctions WHERE auction_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, auctionId);
        return rowsAffected > 0;
    }
}
