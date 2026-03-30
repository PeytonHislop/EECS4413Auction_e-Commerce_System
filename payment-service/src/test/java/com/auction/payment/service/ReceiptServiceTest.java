package com.auction.payment.service;

import com.auction.payment.dto.ReceiptResponse;
import com.auction.payment.model.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptServiceTest {

    private final ReceiptService receiptService = new ReceiptService();

    @Test
    void generateReceipt_buildsReceiptWithCalculatedTotal() {
        Order order = new Order();
        order.setOrderId("ORD-001");
        order.setShippingCost(14.25);
        order.setShippingDays(3);

        ReceiptResponse receipt = receiptService.generateReceipt(order, 120.50);

        assertEquals("ORD-001", receipt.getOrderId());
        assertEquals(120.50, receipt.getItemPrice());
        assertEquals(14.25, receipt.getShippingCost());
        assertEquals(134.75, receipt.getTotal());
        assertEquals(3, receipt.getShippingDays());
    }
}
