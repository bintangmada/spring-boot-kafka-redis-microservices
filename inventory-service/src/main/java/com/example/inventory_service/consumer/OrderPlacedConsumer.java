package com.example.inventory_service.consumer;

import com.example.inventory_service.event.OrderPlacedEvent;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedConsumer {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "notificationTopic", groupId = "inventory-group")
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received OrderPlacedEvent for Order: {}", orderPlacedEvent.getOrderNumber());
        
        inventoryRepository.findBySkuCode(orderPlacedEvent.getSkuCode())
                .ifPresent(inventory -> {
                    // Kurangi Stok di Database
                    int newQuantity = inventory.getQuantity() - orderPlacedEvent.getQuantity();
                    inventory.setQuantity(newQuantity);
                    inventoryRepository.save(inventory);
                    
                    // Update Stok di Redis agar cache tidak basi
                    String cacheKey = "inventory:" + orderPlacedEvent.getSkuCode();
                    redisTemplate.opsForValue().set(cacheKey, newQuantity);
                    
                    log.info("Stock updated for SKU: {}. New quantity: {}", orderPlacedEvent.getSkuCode(), newQuantity);
                });
    }
}
