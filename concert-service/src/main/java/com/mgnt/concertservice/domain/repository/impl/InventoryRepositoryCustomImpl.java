package com.mgnt.concertservice.domain.repository.impl;

import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.entity.QInventory;
import com.mgnt.concertservice.domain.repository.InventoryRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryCustomImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<Inventory> findByConcertIdAndConcertDateId(Long concertId, Long concertDateId) {
        QInventory inventory = QInventory.inventory;
        Inventory foundInventory = queryFactory
                .selectFrom(inventory)
                .where(inventory.concertId.eq(concertId)
                        .and(inventory.concertDateId.eq(concertDateId)))
                .fetchOne();

        return Optional.ofNullable(foundInventory);
    }

    @Override
    @Transactional
    public int updateRemainingInventory(Long concertId, Long concertDateId, Long remaining) {
        QInventory inventory = QInventory.inventory;

        long affectedRows = queryFactory
                .update(inventory)
                .set(inventory.remaining, inventory.remaining.add(remaining))
                .where(inventory.concertId.eq(concertId)
                        .and(inventory.concertDateId.eq(concertDateId))
                        .and(inventory.remaining.gt(0))  // 재고량이 0보다 큰 경우
                        .and(inventory.remaining.add(remaining).goe(0)))  // 업데이트 후에도 재고량이 0 이상이 되는 경우
                .execute();

        return (int) affectedRows;
    }
}
