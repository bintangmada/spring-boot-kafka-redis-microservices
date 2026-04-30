package com.example.order_service.service;

import com.example.order_service.event.OrderPlacedEvent;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final WebClient webClient;

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackPlaceOrder")
    public void placeOrder(OrderRequest orderRequest) {
        // Panggil Inventory Service secara sinkron untuk cek stok
        Boolean isStockAvailable = webClient.get()
                .uri("http://localhost:8082/api/inventory/check",
                        uriBuilder -> uriBuilder.queryParam("skuCode", orderRequest.getSkuCode())
                                .queryParam("quantity", orderRequest.getQuantity())
                                .build())
                .retrieve()
                .bodyToMono(Boolean.class)
                .block(); // .block() membuatnya menjadi sinkron

        if (Boolean.TRUE.equals(isStockAvailable)) {
            Order order = new Order();
            String orderNumber = UUID.randomUUID().toString();
            order.setOrderNumber(orderNumber);
            order.setSkuCode(orderRequest.getSkuCode());
            order.setPrice(orderRequest.getPrice());
            order.setQuantity(orderRequest.getQuantity());

            orderRepository.save(order);

            // Kirim pesan ke Kafka topik "order-placed"
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(orderNumber, order.getSkuCode(), order.getQuantity()));
        } else {
            throw new RuntimeException("Stock not available");
        }
    }

    // Fungsi Cadangan (Fallback) jika Inventory Service Down
    public void fallbackPlaceOrder(OrderRequest orderRequest, RuntimeException runtimeException) {
        throw new RuntimeException("Mohon maaf, layanan pengecekan stok sedang sibuk. Silakan coba lagi nanti.");
    }
}
