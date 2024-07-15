package com.mgnt.concertservice.domain.repository.impl;

import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.entity.QInventory;
import com.mgnt.concertservice.domain.repository.InventoryRepositoryCustom;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
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

    //    @Override
//    public Long updateRemainingInventoryWithPessimisticLock(Long concertId, Long concertDateId, Long remainingChange) {
//        QInventory inventory = QInventory.inventory;
//
//        return queryFactory
//                .update(inventory)
//                .set(inventory.remaining, inventory.remaining.add(remainingChange))
//                .where(inventory.concertId.eq(concertId)
//                        .and(inventory.concertDateId.eq(concertDateId))
//                        .and(inventory.remaining.add(remainingChange).goe(0))) // 재고가 음수가 되지 않도록 체크
//                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
//                .execute();
//    }
    
    @Transactional
    public Long updateRemainingInventoryWithPessimisticLock(Long concertId, Long concertDateId, Long remainingChange) {
        // 1. 비관적 락으로 엔티티를 먼저 조회
        Inventory inventory = queryFactory
                .selectFrom(QInventory.inventory)
                .where(QInventory.inventory.concertId.eq(concertId)
                        .and(QInventory.inventory.concertDateId.eq(concertDateId)))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();

        if (inventory == null) {
            throw new CustomException(ErrorCode.INVENTORY_NOT_FOUND, "Inventory not found.", Level.WARN);
        }

        // 2. 조회된 엔티티의 재고를 업데이트
        if (inventory.getRemaining() + remainingChange >= 0) {
            inventory.setRemaining(inventory.getRemaining() + remainingChange);
            return 1L; // 업데이트 성공
        } else {
            return 0L; // 재고 부족
        }
    }

    @Override
    @Transactional
    public Long updateRemainingInventoryWithOptimisticLock(Long concertId, Long concertDateId, Long remainingChange, Long version) {
        QInventory inventory = QInventory.inventory;

        long updatedRows = queryFactory.update(inventory)
                .set(inventory.remaining, inventory.remaining.add(remainingChange))
                .set(inventory.version, inventory.version.add(1))
                .where(inventory.concertId.eq(concertId)
                        .and(inventory.concertDateId.eq(concertDateId))
                        .and(inventory.version.eq(version)))
                .execute();

        return updatedRows;
    }

    @Override
    public Long findVersionByConcertIdAndConcertDateId(Long concertId, Long concertDateId) {
        QInventory inventory = QInventory.inventory;

        return queryFactory.select(inventory.version)
                .from(inventory)
                .where(inventory.concertId.eq(concertId)
                        .and(inventory.concertDateId.eq(concertDateId)))
                .setLockMode(LockModeType.OPTIMISTIC)
                .fetchOne();
    }

}
