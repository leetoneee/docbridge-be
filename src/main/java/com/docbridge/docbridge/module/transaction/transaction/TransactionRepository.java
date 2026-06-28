package com.docbridge.docbridge.module.transaction.transaction;

import com.docbridge.docbridge.module.transaction.transaction.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // ----------------------------------------------------------------
    // Outbox: giao dịch do unit gửi đi
    // ----------------------------------------------------------------
    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE t.senderUnitId = :unitId
              AND (:keyword IS NULL OR LOWER(t.documentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                    OR LOWER(t.title)        LIKE LOWER(CONCAT('%', :keyword,   '%')))
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
            @Param("unitId") Long unitId,
            @Param("keyword") String keyword,
            @Param("receiverCode") String receiverCode,
            @Param("status") TransactionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    // ----------------------------------------------------------------
    // Inbox: giao dịch gửi đến unit
    // ----------------------------------------------------------------
    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE t.receiverUnitId = :unitId
              AND (:keyword IS NULL OR LOWER(t.documentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                    OR LOWER(t.title)        LIKE LOWER(CONCAT('%', :keyword,   '%')))
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
            @Param("unitId") Long unitId,
            @Param("keyword") String keyword,
            @Param("senderCode") String senderCode,
            @Param("status") TransactionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    Optional<TransactionEntity> findByTransactionCode(String transactionCode);

    @Query(value = """
            SELECT t.status, COUNT(*) as cnt
            FROM `transaction` t
            JOIN interop_unit u ON u.id = t.sender_unit_id
            WHERE (:from IS NULL OR DATE(t.created_at) >= :from)
              AND (:to   IS NULL OR DATE(t.created_at) <= :to)
              AND (:systemId IS NULL OR u.system_id = :systemId)
            GROUP BY t.status
            """, nativeQuery = true)
    List<Object[]> countByStatus(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("systemId") Long systemId);

    @Query(value = """
            SELECT s.id, s.code, s.name, COUNT(*) as cnt
            FROM `transaction` t
            JOIN interop_unit u  ON u.id = t.sender_unit_id
            JOIN interop_system s ON s.id = u.system_id
            WHERE (:from IS NULL OR DATE(t.created_at) >= :from)
              AND (:to   IS NULL OR DATE(t.created_at) <= :to)
            GROUP BY s.id, s.code, s.name
            ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> countBySystem(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query(value = """
            SELECT u.id, u.interop_code, u.name, COUNT(*) as cnt
            FROM `transaction` t
            JOIN interop_unit u ON u.id = t.sender_unit_id
            WHERE (:from IS NULL OR DATE(t.created_at) >= :from)
              AND (:to   IS NULL OR DATE(t.created_at) <= :to)
              AND (:systemId IS NULL OR u.system_id = :systemId)
            GROUP BY u.id, u.interop_code, u.name
            ORDER BY cnt DESC
            LIMIT :topN
            """, nativeQuery = true)
    List<Object[]> countByUnit(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("systemId") Long systemId,
            @Param("topN") int topN);

    @Query(value = """
            SELECT DATE(t.created_at) as period, COUNT(*) as cnt
            FROM `transaction` t
            JOIN interop_unit u ON u.id = t.sender_unit_id
            WHERE (:from IS NULL OR DATE(t.created_at) >= :from)
              AND (:to   IS NULL OR DATE(t.created_at) <= :to)
              AND (:systemId IS NULL OR u.system_id = :systemId)
            GROUP BY DATE(t.created_at)
            ORDER BY period
            """, nativeQuery = true)
    List<Object[]> countByDay(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("systemId") Long systemId);

    @Query(value = """
            SELECT DATE_FORMAT(t.created_at, '%Y-%m') as period, COUNT(*) as cnt
            FROM `transaction` t
            JOIN interop_unit u ON u.id = t.sender_unit_id
            WHERE (:from IS NULL OR DATE(t.created_at) >= :from)
              AND (:to   IS NULL OR DATE(t.created_at) <= :to)
              AND (:systemId IS NULL OR u.system_id = :systemId)
            GROUP BY DATE_FORMAT(t.created_at, '%Y-%m')
            ORDER BY period
            """, nativeQuery = true)
    List<Object[]> countByMonth(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("systemId") Long systemId);
}
