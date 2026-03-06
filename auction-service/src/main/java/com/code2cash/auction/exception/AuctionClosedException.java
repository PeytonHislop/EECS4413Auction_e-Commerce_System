package com.code2cash.auction.exception;

/**
 * Exception thrown when trying to bid on a closed auction
 */
public class AuctionClosedException extends RuntimeException {
    public AuctionClosedException(String message) {
        super(message);
    }
}
