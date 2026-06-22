package com.docbridge.docbridge.module.transaction.transaction;

import com.docbridge.docbridge.module.transaction.transaction.dto.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // ----------------------------------------------------------------
    // Outbox: giao dịch do unit gửi đi
    // ----------------------------------------------------------------
    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE t.senderUnitId = :unitId
              AND (:documentCode IS NULL OR LOWER(t.documentCode) LIKE LOWER(CONCAT('%', :documentCode, '%')))
              AND (:title        IS NULL OR LOWER(t.title)        LIKE LOWER(CONCAT('%', :title,        '%')))
              AND (:receiverCode IS NULL OR EXISTS (
                    SELECT 1 FROM InteropUnitEntity u
                    WHERE u.id = t.receiverUnitId
                      AND LOWER(u.interopCode) LIKE LOWER(CONCAT('%', :receiverCode, '%'))
                  ))
              AND (:status IS NULL OR t.status = :status)
              AND (:from   IS NULL OR t.createdAt >= :from)
              AND (:to     IS NULL OR t.createdAt <= :to)
            ORDER BY t.createdAt DESC
            """)
    Page<TransactionEntity> findOutbox(
            @Param("unitId")       Long unitId,
            @Param("documentCode") String documentCode,
            @Param("title")        String title,
            @Param("receiverCode") String receiverCode,
            @Param("status")       TransactionStatus status,
            @Param("from")         LocalDateTime from,
            @Param("to")           LocalDateTime to,
            Pageable pageable);

    // ----------------------------------------------------------------
    // Inbox: giao dịch gửi đến unit
    // ----------------------------------------------------------------
    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE t.receiverUnitId = :unitId
              AND (:documentCode IS NULL OR LOWER(t.documentCode) LIKE LOWER(CONCAT('%', :documentCode, '%')))
              AND (:title        IS NULL OR LOWER(t.title)        LIKE LOWER(CONCAT('%', :title,        '%')))
              AND (:senderCode   IS NULL OR EXISTS (
                    SELECT 1 FROM InteropUnitEntity u
                    WHERE u.id = t.senderUnitId
                      AND LOWER(u.interopCode) LIKE LOWER(CONCAT('%', :senderCode, '%'))
                  ))
              AND (:status IS NULL OR t.status = :status)
              AND (:from   IS NULL OR t.createdAt >= :from)
              AND (:to     IS NULL OR t.createdAt <= :to)
            ORDER BY t.createdAt DESC
            """)
    Page<TransactionEntity> findInbox(
            @Param("unitId")       Long unitId,
            @Param("documentCode") String documentCode,
            @Param("title")        String title,
            @Param("senderCode")   String senderCode,
            @Param("status")       TransactionStatus status,
            @Param("from")         LocalDateTime from,
            @Param("to")           LocalDateTime to,
            Pageable pageable);

    Optional<TransactionEntity> findByTransactionCode(String transactionCode);
}
