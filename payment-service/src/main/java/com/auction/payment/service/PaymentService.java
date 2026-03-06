package com.auction.payment.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import com.auction.payment.dto.PaymentRequest;
import com.auction.payment.model.Payment;
import com.auction.payment.repository.PaymentRepository;


@Service
public class PaymentService {

	   private final PaymentRepository paymentRepository;

	    public PaymentService(PaymentRepository paymentRepository) {
	        this.paymentRepository = paymentRepository;
	    }

	    public Payment initiatePayment(String userId,
	                                   String auctionId,
	                                   double amount) {

	        Payment payment = new Payment();
	        payment.setUserId(userId);
	        payment.setAuctionId(auctionId);
	        payment.setAmount(amount);
	        payment.setStatus("PENDING");

	        return paymentRepository.save(payment);
	    }

    public Payment processPayment(Payment payment, PaymentRequest request) {

        validatePaymentDetails(request);

        if (!request.getCardNumber().startsWith("4")) {
            payment.setStatus("FAILED");
            return paymentRepository.save(payment);
        }

        payment.setStatus("SUCCESS");
        payment.setTransactionId(UUID.randomUUID().toString());

        return paymentRepository.save(payment);
    }

    private void validatePaymentDetails(PaymentRequest request) {

        if (request.getCardNumber() == null ||
            request.getNameOnCard() == null ||
            request.getExpiry() == null ||
            request.getCvv() == null) {

            throw new RuntimeException("All payment fields must be filled");
        }
    }
}