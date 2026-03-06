package com.code2cash.auction.model;

/**
 * Auction Status Enum
 * Represents the different states an auction can be in
 */
public enum AuctionStatus {
    ACTIVE,      // Auction is currently running
    CLOSED,      // Auction ended with a winner
    NO_SALE,     // Auction ended but reserve price not met
    CANCELLED    // Auction was cancelled by seller
}
