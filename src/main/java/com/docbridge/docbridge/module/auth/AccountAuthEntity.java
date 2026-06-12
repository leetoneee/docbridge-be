package com.docbridge.docbridge.module.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity map bảng `account`, dùng riêng cho module auth.
 * Module account sẽ có AccountEntity riêng đầy đủ hơn.
 *
 * Chỉ khai báo các field cần cho luồng xác thực + đổi mật khẩu.
 */
@Getter
@Setter
@Entity
@Table(name = "account")
public class AccountAuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String status;              // ACTIVE / LOCKED / PENDING

    @Column(name = "is_temp_password", nullable = false)
    private Boolean isTempPassword;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Chỉ cần đọc role, không cần cascade
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleAuthEntity role;
}
