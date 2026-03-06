package com.code2cash.auction.exception;

/**
 * Exception thrown when a bid is invalid
 */
public class InvalidBidException extends RuntimeException {
    public InvalidBidException(String message) {
        super(message);
    }
}
