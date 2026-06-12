package com.docbridge.docbridge.module.auth;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Entity map bảng `role` — chỉ dùng trong module auth.
 */
@Getter
@Entity
@Table(name = "role")
public class RoleAuthEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;    // ADMIN / OPERATOR / UNIT
}
