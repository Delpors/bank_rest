package com.example.bankcards.repository;


import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card,Long> {
    Page<Card> findAllByUserId(Long userId, String search, Pageable pageable);
    Page<Card> findAllByUserId(Long userId, Pageable pageable);
    List<Card> findByUserId(Long userId);

    @Query("SELECT c FROM Card c WHERE " +
            "c.active = true AND " +
            "(LOWER(c.cardNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.cardHolderName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Card> searchCards(@Param("search") String search, Pageable pageable);
}

