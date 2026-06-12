package com.docbridge.docbridge.module.auth;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Entity map bảng `permission` — chỉ dùng trong module auth.
 */
@Getter
@Entity
@Table(name = "permission")
public class PermissionAuthEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;    // VD: SYSTEM_CREATE, UNIT_APPROVE
}
