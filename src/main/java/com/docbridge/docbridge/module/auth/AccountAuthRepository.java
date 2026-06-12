package com.docbridge.docbridge.module.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository dùng riêng cho module auth.
 * Chỉ chứa các query phục vụ xác thực — không expose toàn bộ AccountEntity
 * để tránh coupling với module account.
 *
 * Dùng native projection interface để chỉ SELECT đúng cột cần thiết.
 */
@Repository
public interface AccountAuthRepository extends JpaRepository<AccountAuthEntity, Long> {

    @Query("""
            SELECT a.id          AS id,
                   a.email       AS email,
                   a.password    AS password,
                   a.status      AS status,
                   a.isTempPassword AS isTempPassword,
                   a.unitId      AS unitId,
                   r.code        AS roleCode,
                   r.id          AS roleId
            FROM   AccountAuthEntity a
            JOIN   a.role r
            WHERE  a.email = :email
            """)
    Optional<AccountAuthProjection> findAuthProjectionByEmail(@Param("email") String email);

    @Query("""
            SELECT p.code
            FROM   RolePermissionAuthEntity rp
            JOIN   rp.permission p
            WHERE  rp.roleId = :roleId
            """)
    List<String> findPermissionCodesByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("UPDATE AccountAuthEntity a SET a.lastLoginAt = :at WHERE a.id = :id")
    void updateLastLoginAt(@Param("id") Long id, @Param("at") LocalDateTime at);

    @Modifying
    @Query("""
            UPDATE AccountAuthEntity a
            SET    a.password       = :newPassword,
                   a.isTempPassword = false
            WHERE  a.id = :id
            """)
    void updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword);

    // ----------------------------------------------------------------
    // Projection interface
    // ----------------------------------------------------------------

    interface AccountAuthProjection {
        Long    getId();
        String  getEmail();
        String  getPassword();
        String  getStatus();
        Boolean getIsTempPassword();
        Long    getUnitId();
        String  getRoleCode();
        Long    getRoleId();
    }
}
