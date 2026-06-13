package com.docbridge.docbridge.module.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    boolean existsByEmail(String email);

    Optional<AccountEntity> findByEmail(String email);

    /**
     * Lọc theo roleCode và/hoặc status. Null = bỏ qua filter đó.
     * Mặc định luôn exclude ADMIN khỏi kết quả.
     */
    @Query("""
            SELECT a FROM AccountEntity a
            JOIN RoleSummary r ON r.id = a.roleId
            WHERE r.code <> 'ADMIN'
              AND (:roleCode IS NULL OR r.code = :roleCode)
              AND (:status IS NULL OR a.status = :status)
            """)
    Page<AccountEntity> findByFilter(
            @Param("roleCode") String roleCode,
            @Param("status") AccountStatus status,
            Pageable pageable);
}
