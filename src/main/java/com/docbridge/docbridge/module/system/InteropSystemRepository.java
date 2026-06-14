package com.docbridge.docbridge.module.system;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InteropSystemRepository extends JpaRepository<InteropSystemEntity, Long> {

    boolean existsByCode(String code);

    // Tìm theo id, chưa bị soft delete
    Optional<InteropSystemEntity> findById(Long id);

    // Kiểm tra hệ thống có đơn vị nào không (dùng cho UC1.6)
    @Query("SELECT COUNT(u) > 0 FROM InteropUnitEntity u WHERE u.system.id = :systemId")
    boolean existsUnitBySystemId(@Param("systemId") Long systemId);

    // Danh sách có filter (UC1.2 / UC1.7)
    @Query("""
            SELECT s FROM InteropSystemEntity s
            WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:status IS NULL OR s.status = :status)
            """)
    Page<InteropSystemEntity> findAllWithFilter(
            @Param("name") String name,
            @Param("status") InteropSystemStatus status,
            Pageable pageable);
}
