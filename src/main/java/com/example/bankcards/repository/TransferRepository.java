package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByFromCardId(Long fromCardId);

    List<Transfer> findByToCardId(Long toCardId);

    @Query("SELECT t FROM Transfer t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId")
    Page<Transfer> findByFromCardIdOrToCardId(@Param("cardId") Long cardId, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.fromCard.user.id = :userId OR t.toCard.user.id = :userId")
    Page<Transfer> findByUserId(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT t FROM Transfer t " +
            "JOIN t.fromCard fc " +
            "JOIN t.toCard tc " +
            "WHERE fc.user.id = :userId OR tc.user.id = :userId")
    Page<Transfer> findAllUserTransfers(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT t FROM Transfer t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId " +
            "ORDER BY t.createdAt DESC")
    Page<Transfer> findByCardId(@Param("cardId") Long cardId, Pageable pageable);
}