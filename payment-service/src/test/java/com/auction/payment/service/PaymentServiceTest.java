package com.auction.payment.service;

import com.auction.payment.dto.PaymentRequest;
import com.auction.payment.model.Payment;
import com.auction.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void initiatePayment_setsPendingStatusAndSavesPayment() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment saved = paymentService.initiatePayment("buyer-1", "AUC001", 125.75);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());

        Payment payment = captor.getValue();
        assertEquals("buyer-1", payment.getUserId());
        assertEquals("AUC001", payment.getAuctionId());
        assertEquals(125.75, payment.getAmount());
        assertEquals("PENDING", payment.getStatus());

        assertEquals("buyer-1", saved.getUserId());
        assertEquals("AUC001", saved.getAuctionId());
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    void processPayment_withVisaLikeCard_marksPaymentSuccessful() {
        Payment payment = new Payment();
        payment.setStatus("PENDING");

        PaymentRequest request = validRequest();
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.processPayment(payment, request);

        verify(paymentRepository).save(payment);
        assertEquals("SUCCESS", result.getStatus());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().length() > 10);
    }

    @Test
    void processPayment_withNonVisaLikeCard_marksPaymentFailed() {
        Payment payment = new Payment();
        payment.setStatus("PENDING");

        PaymentRequest request = validRequest();
        request.setCardNumber("5111111111111111");

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.processPayment(payment, request);

        verify(paymentRepository).save(payment);
        assertEquals("FAILED", result.getStatus());
        assertNull(result.getTransactionId());
    }

    @Test
    void processPayment_withMissingPaymentField_throwsExceptionAndDoesNotSave() {
        Payment payment = new Payment();
        PaymentRequest request = validRequest();
        request.setCvv(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.processPayment(payment, request));

        assertEquals("All payment fields must be filled", exception.getMessage());
        verify(paymentRepository, times(0)).save(any(Payment.class));
    }

    private PaymentRequest validRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setAuctionId("AUC001");
        request.setCardNumber("4111111111111111");
        request.setNameOnCard("Taylor Buyer");
        request.setExpiry("12/27");
        request.setCvv("123");
        return request;
    }
}
