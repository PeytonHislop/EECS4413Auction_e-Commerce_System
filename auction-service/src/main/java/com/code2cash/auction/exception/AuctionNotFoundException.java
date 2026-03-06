package com.code2cash.auction.exception;

/**
 * Exception thrown when an auction is not found
 */
public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(String message) {
        super(message);
    }
    
    public AuctionNotFoundException(String auctionId, String reason) {
        super(String.format("Auction %s not found: %s", auctionId, reason));
    }
}
