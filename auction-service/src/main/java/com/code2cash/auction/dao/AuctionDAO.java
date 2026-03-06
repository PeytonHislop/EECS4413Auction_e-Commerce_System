package com.code2cash.auction.dao;

import com.code2cash.auction.model.Auction;
import com.code2cash.auction.model.AuctionStatus;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Auction operations
 * Defines all database operations for auctions
 */
public interface AuctionDAO {
    
    /**
     * Create a new auction in the database
     * @param auction The auction to create
     * @return The created auction with generated ID
     */
    Auction createAuction(Auction auction);
    
    /**
     * Find an auction by its ID
     * @param auctionId The auction ID
     * @return Optional containing the auction if found
     */
    Optional<Auction> findById(String auctionId);
    
    /**
     * Get all active auctions (status = ACTIVE and not expired)
     * @return List of active auctions
     */
    List<Auction> findActiveAuctions();
    
    /**
     * Get all auctions by status
     * @param status The auction status
     * @return List of auctions with the given status
     */
    List<Auction> findByStatus(AuctionStatus status);
    
    /**
     * Get all auctions for a specific seller
     * @param sellerId The seller's user ID
     * @return List of auctions by this seller
     */
    List<Auction> findBySellerId(String sellerId);
    
    /**
     * Update an existing auction
     * @param auction The auction to update
     * @return true if update was successful
     */
    boolean updateAuction(Auction auction);
    
    /**
     * Update the current highest bid for an auction
     * @param auctionId The auction ID
     * @param bidAmount The new highest bid amount
     * @param bidderId The bidder's user ID
     * @return true if update was successful
     */
    boolean updateHighestBid(String auctionId, java.math.BigDecimal bidAmount, String bidderId);
    
    /**
     * Close an auction and set the winner
     * @param auctionId The auction ID
     * @param winnerId The winner's user ID (null if no winner)
     * @param status The final status (CLOSED or NO_SALE)
     * @return true if update was successful
     */
    boolean closeAuction(String auctionId, String winnerId, AuctionStatus status);
    
    /**
     * Get auctions that have ended but are still marked as ACTIVE
     * Used by the scheduler to close expired auctions
     * @return List of expired auctions
     */
    List<Auction> findExpiredActiveAuctions();
    
    /**
     * Delete an auction (for testing purposes mainly)
     * @param auctionId The auction ID
     * @return true if deletion was successful
     */
    boolean deleteAuction(String auctionId);
}
