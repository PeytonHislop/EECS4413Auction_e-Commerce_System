package com.auction.payment.controller;

import com.auction.payment.dto.PaymentRequest;
import com.auction.payment.dto.ReceiptResponse;
import com.auction.payment.exception.UnauthorizedRoleException;
import com.auction.payment.exception.UserMismatchException;
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
            @RequestHeader("X-User-Id") String gatewayUserId,
            @RequestHeader("X-Role") String gatewayRole,
            @RequestHeader("X-Shipping-Address") String shippingAddress,
            @Valid @RequestBody PaymentRequest request
    ) {

        validateGatewayIdentity(gatewayUserId, gatewayRole, request);

        Payment payment = paymentService.initiatePayment(
                request.getUserId(),
                request.getAuctionId(),
                request.getAmount()
        );

        Order order = orderService.createOrder(payment, 5.99, 3);

        ReceiptResponse receipt =
                receiptService.generateReceipt(order, request.getAmount());

        receipt.setShippingAddress(shippingAddress);

        return receipt;
    }

    private void validateGatewayIdentity(
            String gatewayUserId,
            String gatewayRole,
            PaymentRequest request
    ) {
        if (!gatewayUserId.equals(request.getUserId())) {
            throw new UserMismatchException(
                    "User mismatch between gateway identity and request"
            );
        }

        if (!"BUYER".equalsIgnoreCase(gatewayRole)) {
            throw new UnauthorizedRoleException(
                    "User does not have BUYER role"
            );
        }
    }
}