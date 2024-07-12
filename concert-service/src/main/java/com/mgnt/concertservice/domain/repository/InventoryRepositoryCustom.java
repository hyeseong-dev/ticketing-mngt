package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Inventory;

import java.util.Optional;

public interface InventoryRepositoryCustom {

    Optional<Inventory> findByConcertIdAndConcertDateId(Long concertId, Long concertDateId);

    int updateRemainingInventory(Long concertId, Long concertDateId, Long remaining);
}
