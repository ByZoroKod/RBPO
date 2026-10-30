package org.example.repository;

import jakarta.persistence.LockModeType;
import org.example.model.AuctionLot;
import org.example.model.LotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuctionLotRepository extends JpaRepository<AuctionLot, Long> {

    boolean existsByItemIdAndStatus(Long itemId, LotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from AuctionLot l where l.id = :id")
    Optional<AuctionLot> findByIdForUpdate(@Param("id") Long id);
}
