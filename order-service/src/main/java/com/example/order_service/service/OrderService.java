package com.example.order_service.service;

import com.example.order_service.dto.OrderRequest;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setSkuCode(orderRequest.getSkuCode());
        order.setPrice(orderRequest.getPrice());
        order.setQuantity(orderRequest.getQuantity());

        orderRepository.save(order);
        // Nanti di sini kita akan tambahkan logika Kafka untuk memberitahu Inventory
    }
}
