package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, InventoryRepositoryCustom {

}
