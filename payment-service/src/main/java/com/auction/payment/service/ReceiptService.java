package com.auction.payment.service;


import com.auction.payment.dto.ReceiptResponse;
import com.auction.payment.model.Order;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService {

    public ReceiptResponse generateReceipt(Order order, double itemPrice) {

        double total = itemPrice + order.getShippingCost();

        return new ReceiptResponse(
                order.getOrderId(),
                itemPrice,
                order.getShippingCost(),
                total,
                order.getShippingDays()
        );
    }
}
