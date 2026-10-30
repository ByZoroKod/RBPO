package org.example.repository;

import org.example.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findFirstByLotIdOrderByAmountDescIdAsc(Long lotId);
}
