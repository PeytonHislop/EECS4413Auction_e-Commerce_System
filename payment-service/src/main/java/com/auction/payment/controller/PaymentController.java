package com.auction.payment.controller;

import com.auction.payment.dto.PaymentRequest;
import com.auction.payment.dto.ReceiptResponse;
import com.auction.payment.exception.UnauthorizedRoleException;
import com.auction.payment.model.Order;
import com.auction.payment.model.Payment;
import com.auction.payment.service.OrderService;
import com.auction.payment.service.PaymentService;
import com.auction.payment.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ReceiptService receiptService;

    public PaymentController(
            PaymentService paymentService,
            OrderService orderService,
            ReceiptService receiptService
    ) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.receiptService = receiptService;
    }

    @PostMapping("/process")
    @ResponseStatus(HttpStatus.OK)
    public ReceiptResponse processPayment(
            @RequestHeader("X-User-Id") String winnerUserId,
            @RequestHeader("X-Role") String gatewayRole,
            @RequestHeader("X-Shipping-Address") String shippingAddress,
            @RequestHeader("X-Shipping-Cost") double shippingCost,
            @RequestHeader("X-Sold-Price") double soldPrice,
            @Valid @RequestBody PaymentRequest request
    ) {
        if (!"BUYER".equalsIgnoreCase(gatewayRole)) {
            throw new UnauthorizedRoleException("User does not have BUYER role");
        }

        Payment payment = paymentService.initiatePayment(
                winnerUserId,
                request.getAuctionId(),
                soldPrice
        );

        Order order = orderService.createOrder(payment, shippingCost, 3);

        ReceiptResponse receipt =
                receiptService.generateReceipt(order, soldPrice);

        receipt.setShippingAddress(shippingAddress);

        return receipt;
    }
}