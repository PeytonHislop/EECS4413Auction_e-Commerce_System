package com.code2cash.auction.dao;

import com.code2cash.auction.model.Bid;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Bid operations
 * Defines all database operations for bids
 */
public interface BidDAO {
    
    /**
     * Create a new bid in the database
     * @param bid The bid to create
     * @return The created bid with generated ID
     */
    Bid createBid(Bid bid);
    
    /**
     * Find a bid by its ID
     * @param bidId The bid ID
     * @return Optional containing the bid if found
     */
    Optional<Bid> findById(String bidId);
    
    /**
     * Get all bids for a specific auction
     * Ordered by timestamp (newest first)
     * @param auctionId The auction ID
     * @return List of bids for this auction
     */
    List<Bid> findByAuctionId(String auctionId);
    
    /**
     * Get all bids by a specific bidder
     * @param bidderId The bidder's user ID
     * @return List of bids by this bidder
     */
    List<Bid> findByBidderId(String bidderId);
    
    /**
     * Get the highest bid for a specific auction
     * @param auctionId The auction ID
     * @return Optional containing the highest bid if any bids exist
     */
    Optional<Bid> findHighestBidByAuctionId(String auctionId);
    
    /**
     * Get the bid count for an auction
     * @param auctionId The auction ID
     * @return Number of bids placed on this auction
     */
    int getBidCount(String auctionId);
    
    /**
     * Delete all bids for an auction (cascade when auction is deleted)
     * @param auctionId The auction ID
     * @return Number of bids deleted
     */
    int deleteBidsByAuctionId(String auctionId);
}
