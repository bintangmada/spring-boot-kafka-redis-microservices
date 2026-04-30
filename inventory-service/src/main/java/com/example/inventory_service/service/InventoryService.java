package com.example.inventory_service.service;

import com.example.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode, Integer quantity) {
        String cacheKey = "inventory:" + skuCode;
        
        // 1. Coba ambil dari Redis Cache
        Integer cachedStock = (Integer) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedStock != null) {
            log.info("Stock info found in Redis for SKU: {}", skuCode);
            return cachedStock >= quantity;
        }

        // 2. Jika tidak ada di Redis, cari di Database
        log.info("Stock info not found in Redis, checking Database for SKU: {}", skuCode);
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory -> {
                    // Simpan hasil ke Redis selama 10 menit
                    redisTemplate.opsForValue().set(cacheKey, inventory.getQuantity(), Duration.ofMinutes(10));
                    return inventory.getQuantity() >= quantity;
                })
                .orElse(false);
    }
}
