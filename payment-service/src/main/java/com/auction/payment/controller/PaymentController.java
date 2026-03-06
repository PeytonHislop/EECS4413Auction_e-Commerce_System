package com.auction.payment.controller;

import com.auction.payment.client.IamServiceClient;
import com.auction.payment.dto.AddressDto;
import com.auction.payment.dto.PaymentRequest;
import com.auction.payment.dto.ReceiptResponse;
import com.auction.payment.dto.UserProfileResponse;
import com.auction.payment.dto.ValidateTokenResponse;
import com.auction.payment.exception.InvalidTokenException;
import com.auction.payment.exception.UserMismatchException;
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
    private final IamServiceClient iamServiceClient;

    public PaymentController(
            PaymentService paymentService,
            OrderService orderService,
            ReceiptService receiptService,
            IamServiceClient iamServiceClient) {

        this.paymentService = paymentService;
        this.orderService = orderService;
        this.receiptService = receiptService;
        this.iamServiceClient = iamServiceClient;
    }

    @PostMapping("/process")
    @ResponseStatus(HttpStatus.OK)
    public ReceiptResponse processPayment(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PaymentRequest request) {

        /*
         * Step 1: Validate JWT with IAM service
         */
        ValidateTokenResponse tokenResponse =
                iamServiceClient.validateToken(authHeader);

        if (!tokenResponse.isValid()) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        String userId = tokenResponse.getUserId();

        /*
         * Step 2: Ensure the request user matches the token user
         */
        if (!userId.equals(request.getUserId())) {
            throw new UserMismatchException(
                    "User mismatch between token and request");
        }

        /*
         * Step 3: Verify user role is BUYER
         */
        boolean authorized =
                iamServiceClient.authorizeRole(authHeader, "BUYER");

        if (!authorized) {
            throw new UnauthorizedRoleException(
                    "User does not have BUYER role");
        }

        /*
         * Step 4: Retrieve user profile (shipping address)
         */
        UserProfileResponse userProfile =
                iamServiceClient.getUserProfile(authHeader, userId);

        AddressDto address = userProfile.getShippingAddress();

        String shippingAddress =
                address.getStreetNumber() + " " +
                address.getStreetName() + ", " +
                address.getCity() + ", " +
                address.getCountry() + ", " +
                address.getPostalCode();

        /*
         * Step 5: Process payment
         */
        Payment payment = paymentService.initiatePayment(
                request.getUserId(),
                request.getAuctionId(),
                request.getAmount()
        );

        /*
         * Step 6: Create order
         */
        Order order = orderService.createOrder(payment, 5.99, 3);

        /*
         * Step 7: Generate receipt
         */
        ReceiptResponse receipt =
                receiptService.generateReceipt(order, request.getAmount());

        receipt.setShippingAddress(shippingAddress);

        return receipt;
    }
}