package com.docbridge.docbridge.module.permission.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @Query("""
            SELECT r FROM RoleEntity r
            LEFT JOIN FETCH r.rolePermissions rp
            LEFT JOIN FETCH rp.permission
            WHERE r.id = :id
            """)
    Optional<RoleEntity> findByIdWithPermissions(@Param("id") Long id);
}
