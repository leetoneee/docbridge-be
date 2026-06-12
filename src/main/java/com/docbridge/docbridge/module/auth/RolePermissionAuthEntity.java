package com.docbridge.docbridge.module.auth;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Entity map bảng `role_permission` — chỉ dùng trong module auth
 * để query permission codes theo roleId.
 */
@Getter
@Entity
@Table(name = "role_permission")
public class RolePermissionAuthEntity {

    @Id
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionAuthEntity permission;
}
