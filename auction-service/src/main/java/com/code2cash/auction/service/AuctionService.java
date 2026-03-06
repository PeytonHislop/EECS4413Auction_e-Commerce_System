package com.code2cash.auction.service;

import com.code2cash.auction.dto.AuctionResponse;
import com.code2cash.auction.dto.CreateAuctionRequest;
import com.code2cash.auction.model.Auction;
import java.util.List;

/**
 * Service interface for Auction business logic
 * This layer sits between Controllers and DAOs
 */
public interface AuctionService {
    
    /**
     * Create a new auction
     * Validates seller credentials, item existence, and input data
     * 
     * @param request The auction creation request
     * @param token Authentication token from user
     * @return The created auction response
     */
    AuctionResponse createAuction(CreateAuctionRequest request, String token);
    
    /**
     * Get auction details by ID
     * 
     * @param auctionId The auction ID
     * @return Auction response with all details
     */
    AuctionResponse getAuction(String auctionId);
    
    /**
     * Get all active auctions
     * 
     * @return List of active auction responses
     */
    List<AuctionResponse> getActiveAuctions();
    
    /**
     * Get all auctions for a specific seller
     * 
     * @param sellerId The seller's user ID
     * @return List of seller's auctions
     */
    List<AuctionResponse> getSellerAuctions(String sellerId);
    
    /**
     * Close an auction and determine winner
     * Called by the scheduler when auction time expires
     * 
     * @param auctionId The auction ID to close
     * @return Updated auction response
     */
    AuctionResponse closeAuction(String auctionId);
    
    /**
     * Manually close all expired auctions
     * Called by the scheduler periodically
     * 
     * @return Number of auctions closed
     */
    int closeExpiredAuctions();
}
