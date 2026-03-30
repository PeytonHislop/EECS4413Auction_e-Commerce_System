package com.auction.payment;

import com.auction.payment.model.Order;
import com.auction.payment.model.Payment;
import com.auction.payment.repository.OrderRepository;
import com.auction.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void processPayment_persistsPaymentAndOrder_andReturnsReceipt() throws Exception {
        mockMvc.perform(post("/payments/process")
                        .header("X-User-Id", "buyer-1")
                        .header("X-Role", "BUYER")
                        .header("X-Shipping-Address", "123 Main St, Toronto, Canada, M1M1M1")
                        .header("X-Shipping-Cost", "15.25")
                        .header("X-Sold-Price", "120.50")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "auctionId": "AUC001",
                                  "cardNumber": "4111111111111111",
                                  "nameOnCard": "Taylor Buyer",
                                  "expiry": "12/27",
                                  "cvv": "123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.itemPrice").value(120.50))
                .andExpect(jsonPath("$.shippingCost").value(15.25))
                .andExpect(jsonPath("$.total").value(135.75))
                .andExpect(jsonPath("$.shippingDays").value(3))
                .andExpect(jsonPath("$.shippingAddress").value("123 Main St, Toronto, Canada, M1M1M1"));

        List<Payment> payments = paymentRepository.findAll();
        assertEquals(1, payments.size());

        Payment payment = payments.get(0);
        assertNotNull(payment.getPaymentId());
        assertEquals("buyer-1", payment.getUserId());
        assertEquals("AUC001", payment.getAuctionId());
        assertEquals(120.50, payment.getAmount());
        assertEquals("PENDING", payment.getStatus());

        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());

        Order order = orders.get(0);
        assertNotNull(order.getOrderId());
        assertEquals(payment.getPaymentId(), order.getPaymentId());
        assertEquals("buyer-1", order.getUserId());
        assertEquals("AUC001", order.getAuctionId());
        assertEquals(15.25, order.getShippingCost());
        assertEquals(3, order.getShippingDays());
        assertEquals("CREATED", order.getStatus());
    }

    @Test
    void processPayment_withUnauthorizedRole_returnsForbidden_andDoesNotPersistData() throws Exception {
        mockMvc.perform(post("/payments/process")
                        .header("X-User-Id", "buyer-1")
                        .header("X-Role", "SELLER")
                        .header("X-Shipping-Address", "123 Main St, Toronto, Canada, M1M1M1")
                        .header("X-Shipping-Cost", "15.25")
                        .header("X-Sold-Price", "120.50")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "auctionId": "AUC001",
                                  "cardNumber": "4111111111111111",
                                  "nameOnCard": "Taylor Buyer",
                                  "expiry": "12/27",
                                  "cvv": "123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("User does not have BUYER role"));

        assertTrue(paymentRepository.findAll().isEmpty());
        assertTrue(orderRepository.findAll().isEmpty());
    }
}
