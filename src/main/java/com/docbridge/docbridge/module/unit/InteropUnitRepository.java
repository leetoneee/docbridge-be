package com.docbridge.docbridge.module.unit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InteropUnitRepository extends JpaRepository<InteropUnitEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<InteropUnitEntity> findByEmail(String email);

    // Đếm số đơn vị ACTIVE trong hệ thống để sinh STT cho interop_code
    @Query("SELECT COUNT(u) FROM InteropUnitEntity u WHERE u.system.id = :systemId AND u.status = 'ACTIVE'")
    long countActiveBySystemId(@Param("systemId") Long systemId);

    // Kiểm tra đơn vị có giao dịch nào chưa (để block xoá)
    @Query("""
        SELECT COUNT(t) > 0
        FROM TransactionEntity t
        WHERE t.senderUnitId = :unitId OR t.receiverUnitId = :unitId
        """)
    boolean hasAnyTransaction(@Param("unitId") Long unitId);

    // Dropdown — chỉ lấy ACTIVE, tìm theo tên hoặc mã liên thông
    @Query("""
        SELECT u FROM InteropUnitEntity u
        WHERE u.status = 'ACTIVE'
          AND (:keyword IS NULL
               OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.interopCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY u.name ASC
        """)
    List<InteropUnitEntity> findAllForDropdown(@Param("keyword") String keyword);

    // Filter + search (UC2.2, UC2.8)
    @Query("""
        SELECT u FROM InteropUnitEntity u
        WHERE (:systemId IS NULL OR u.system.id = :systemId)
          AND (:status IS NULL OR u.status = :status)
          AND (:keyword IS NULL
               OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.interopCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<InteropUnitEntity> findByFilter(
            @Param("systemId") Long systemId,
            @Param("status") InteropUnitStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
