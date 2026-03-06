package com.auction.payment.service;

import com.auction.payment.model.Order;
import com.auction.payment.model.Payment;
import com.auction.payment.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Payment payment,
                             double shippingCost,
                             int shippingDays) {

        Order order = new Order();
        order.setPaymentId(payment.getPaymentId());
        order.setUserId(payment.getUserId());
        order.setAuctionId(payment.getAuctionId());
        order.setShippingCost(shippingCost);
        order.setShippingDays(shippingDays);
        order.setStatus("CREATED");

        return orderRepository.save(order);
    }
//    public Optional<Order> getOrder(String orderId) {
//        return orderRepository.findById(orderId);
//    }
    
}