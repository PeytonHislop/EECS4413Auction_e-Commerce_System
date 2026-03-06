package com.auction.payment.service;


import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    public double calculateShippingCost(String auctionId) {
        return 5.99;
    }

    public int getShippingTime(String auctionId) {
        return 3;
    }
}