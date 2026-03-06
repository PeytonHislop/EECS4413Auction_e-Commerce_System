package com.auction.payment.exception;

public class UserMismatchException extends RuntimeException {

    public UserMismatchException(String message) {
        super(message);
    }
}
