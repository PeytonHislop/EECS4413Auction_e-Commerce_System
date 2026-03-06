package com.code2cash.auction.dao;

import com.code2cash.auction.model.Bid;
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
 * Implementation of BidDAO using Spring JdbcTemplate
 * Handles all database operations for bids using SQLite
 */
@Repository
public class BidDAOImpl implements BidDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // RowMapper to convert ResultSet to Bid object
    private final RowMapper<Bid> bidRowMapper = new RowMapper<Bid>() {
        @Override
        public Bid mapRow(ResultSet rs, int rowNum) throws SQLException {
            Bid bid = new Bid();
            bid.setBidId(rs.getString("bid_id"));
            bid.setAuctionId(rs.getString("auction_id"));
            bid.setBidderId(rs.getString("bidder_id"));
            
            BigDecimal amount = rs.getBigDecimal("bid_amount");
            if (amount != null) {
                bid.setBidAmount(amount);
            }
            
            Timestamp timestamp = rs.getTimestamp("bid_timestamp");
            if (timestamp != null) {
                bid.setBidTimestamp(timestamp.toLocalDateTime());
            }
            
            return bid;
        }
    };
    
    @Override
    public Bid createBid(Bid bid) {
        // Generate ID if not present
        if (bid.getBidId() == null || bid.getBidId().isEmpty()) {
            bid.setBidId(IdGenerator.generateBidId());
        }
        
        // Set timestamp if not present
        if (bid.getBidTimestamp() == null) {
            bid.setBidTimestamp(LocalDateTime.now());
        }
        
        String sql = "INSERT INTO bids (bid_id, auction_id, bidder_id, bid_amount, bid_timestamp) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            bid.getBidId(),
            bid.getAuctionId(),
            bid.getBidderId(),
            bid.getBidAmount(),
            Timestamp.valueOf(bid.getBidTimestamp())
        );
        
        return bid;
    }
    
    @Override
    public Optional<Bid> findById(String bidId) {
        String sql = "SELECT * FROM bids WHERE bid_id = ?";
        try {
            Bid bid = jdbcTemplate.queryForObject(sql, bidRowMapper, bidId);
            return Optional.ofNullable(bid);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Bid> findByAuctionId(String auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_timestamp DESC";
        return jdbcTemplate.query(sql, bidRowMapper, auctionId);
    }
    
    @Override
    public List<Bid> findByBidderId(String bidderId) {
        String sql = "SELECT * FROM bids WHERE bidder_id = ? ORDER BY bid_timestamp DESC";
        return jdbcTemplate.query(sql, bidRowMapper, bidderId);
    }
    
    @Override
    public Optional<Bid> findHighestBidByAuctionId(String auctionId) {
        String sql = "SELECT * FROM bids WHERE auction_id = ? " +
                     "ORDER BY bid_amount DESC, bid_timestamp DESC LIMIT 1";
        try {
            Bid bid = jdbcTemplate.queryForObject(sql, bidRowMapper, auctionId);
            return Optional.ofNullable(bid);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public int getBidCount(String auctionId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE auction_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, auctionId);
        return count != null ? count : 0;
    }
    
    @Override
    public int deleteBidsByAuctionId(String auctionId) {
        String sql = "DELETE FROM bids WHERE auction_id = ?";
        return jdbcTemplate.update(sql, auctionId);
    }
}
