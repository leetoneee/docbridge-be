package com.docbridge.docbridge.module.transaction.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Chỉ đọc — module transaction không write vào interop_unit / interop_system.
 * Dùng entity giả (InteropUnitReadEntity) để Spring Data có thể bind.
 */
@Repository
public interface UnitTransactionRepository
        extends JpaRepository<com.docbridge.docbridge.module.unit.InteropUnitEntity, Long> {

    @Query("""
            SELECT u.id            AS id,
                   u.interopCode   AS interopCode,
                   u.name          AS name,
                   u.status        AS status,
                   u.system.id     AS systemId
            FROM InteropUnitEntity u
            WHERE u.interopCode = :interopCode
            """)
    Optional<UnitTransactionSummary> findSummaryByInteropCode(@Param("interopCode") String interopCode);

    @Query("""
            SELECT u.id             AS id,
                   u.interopCode    AS interopCode,
                   u.name           AS name,
                   u.status         AS status,
                   u.system.id      AS systemId
            FROM InteropUnitEntity u
            WHERE u.id = :id
            """)
    Optional<UnitTransactionSummary> findSummaryById(@Param("id") Long id);
}
