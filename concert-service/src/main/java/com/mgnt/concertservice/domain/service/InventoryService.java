package com.mgnt.concertservice.domain.service;

import com.mgnt.concertservice.domain.entity.Inventory;

import java.util.Optional;

public interface InventoryService {

    boolean updateInventory(Long concertId, Long concertDateId, Long change);

    Optional<Inventory> getInventory(Long concertId, Long concertDateId);

    boolean reserveInventory(Long concertId, Long concertDateId);

    boolean releaseInventory(Long concertId, Long concertDateId);
}