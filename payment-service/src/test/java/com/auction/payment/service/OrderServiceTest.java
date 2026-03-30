package com.auction.payment.service;

import com.auction.payment.model.Order;
import com.auction.payment.model.Payment;
import com.auction.payment.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_copiesPaymentFieldsAndMarksOrderCreated() {
        Payment payment = new Payment();
        payment.setPaymentId("PAY-001");
        payment.setUserId("buyer-1");
        payment.setAuctionId("AUC001");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(payment, 18.50, 3);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order order = captor.getValue();
        assertEquals("PAY-001", order.getPaymentId());
        assertEquals("buyer-1", order.getUserId());
        assertEquals("AUC001", order.getAuctionId());
        assertEquals(18.50, order.getShippingCost());
        assertEquals(3, order.getShippingDays());
        assertEquals("CREATED", order.getStatus());

        assertEquals("PAY-001", result.getPaymentId());
        assertEquals("CREATED", result.getStatus());
    }
}
