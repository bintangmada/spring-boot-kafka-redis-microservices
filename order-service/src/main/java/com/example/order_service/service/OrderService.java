package com.example.order_service.service;

import com.example.order_service.event.OrderPlacedEvent;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        String orderNumber = UUID.randomUUID().toString();
        order.setOrderNumber(orderNumber);
        order.setSkuCode(orderRequest.getSkuCode());
        order.setPrice(orderRequest.getPrice());
        order.setQuantity(orderRequest.getQuantity());

        orderRepository.save(order);

        // Kirim pesan ke Kafka topik "order-placed"
        kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(orderNumber, order.getSkuCode(), order.getQuantity()));
    }
}
