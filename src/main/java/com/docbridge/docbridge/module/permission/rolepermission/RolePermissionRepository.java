package com.docbridge.docbridge.module.permission.rolepermission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    Optional<RolePermissionEntity> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    @Modifying
    @Query("DELETE FROM RolePermissionEntity rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    int deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Query("SELECT rp.permission.code FROM RolePermissionEntity rp WHERE rp.role.id = :roleId")
    List<String> findPermissionCodesByRoleId(@Param("roleId") Long roleId);
}
