package org.example.repository;

import jakarta.persistence.LockModeType;
import org.example.model.AuctionDeal;
import org.example.model.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuctionDealRepository extends JpaRepository<AuctionDeal, Long> {

    boolean existsByLotId(Long lotId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from AuctionDeal d where d.id = :id")
    Optional<AuctionDeal> findByIdForUpdate(@Param("id") Long id);

    @Query("select count(d) > 0 from AuctionDeal d join AuctionLot l on l.id = d.lotId "
            + "where l.itemId = :itemId and d.transferStatus = :status")
    boolean existsByItemIdAndStatus(@Param("itemId") Long itemId, @Param("status") DealStatus status);

    @Query("select d from AuctionDeal d where d.sellerId = :userId "
            + "or exists (select 1 from Bid b where b.id = d.winningBidId and b.bidderId = :userId)")
    List<AuctionDeal> findByParticipant(@Param("userId") Long userId);
}
