package com.mgnt.concertservice.domain.service.impl;

import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.repository.InventoryRepository;
import com.mgnt.concertservice.domain.repository.RedisRepository;
import com.mgnt.concertservice.domain.service.InventoryService;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RedisRepository redisRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public boolean updateInventory(Long concertId, Long concertDateId, Long change) {
        boolean redisSuccess = redisRepository.updateInventory(concertId, concertDateId, change);
        if (!redisSuccess) {
            return false;
        }

        boolean mysqlSuccess = updateMysqlInventory(concertId, concertDateId, change);
        if (!mysqlSuccess) {
            // Redis 롤백
            redisRepository.updateInventory(concertId, concertDateId, -change);
            return false;
        }

        return true;
    }

    @Override
    public Optional<Inventory> getInventory(Long concertId, Long concertDateId) {
        Optional<Inventory> redisInventory = redisRepository.getInventory(concertId, concertDateId);
        if (redisInventory.isPresent()) {
            return redisInventory;
        }

        Optional<Inventory> mysqlInventory = inventoryRepository.findByConcertIdAndConcertDateId(concertId, concertDateId);
        mysqlInventory.ifPresent(inventory -> redisRepository.saveInventory(concertId, concertDateId, inventory));

        return mysqlInventory;
    }

    @Override
    @Transactional
    public boolean reserveInventory(Long concertId, Long concertDateId) {
        return updateInventory(concertId, concertDateId, -1L);
    }

    @Override
    @Transactional
    public boolean releaseInventory(Long concertId, Long concertDateId) {
        return updateInventory(concertId, concertDateId, 1L);
    }

    private boolean updateMysqlInventory(Long concertId, Long concertDateId, Long change) {
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                Inventory inventory = inventoryRepository.findByConcertIdAndConcertDateId(concertId, concertDateId)
                        .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND, "Inventory not found", Level.WARN));

                Long updatedRows = inventoryRepository.updateRemainingInventoryWithOptimisticLock(
                        concertId, concertDateId, change, inventory.getVersion());

                if (updatedRows > 0) {
                    log.info("Successfully updated inventory in MySQL: concertId={}, concertDateId={}, change={}",
                            concertId, concertDateId, change);
                    return true;
                } else {
                    log.warn("Inventory update failed in MySQL due to concurrent modification. Retrying... (Attempt {})", attempt + 1);
                }
            } catch (Exception e) {
                log.warn("Error updating inventory in MySQL. Retrying... (Attempt {})", attempt + 1, e);
                if (attempt == maxRetries - 1) {
                    log.error("Failed to update inventory in MySQL after {} attempts", maxRetries);
                    return false;
                }
            }
        }
        return false;
    }
}