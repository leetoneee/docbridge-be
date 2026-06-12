package com.docbridge.docbridge.module.permission.permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    List<PermissionEntity> findAllByOrderByGroupNameAscCodeAsc();
}
