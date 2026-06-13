package com.docbridge.docbridge.module.account;

import com.docbridge.docbridge.shared.kernel.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * Chỉ có giá trị với tài khoản UNIT.
     * Null với ADMIN / OPERATOR.
     */
    @Column(name = "unit_id")
    private Long unitId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_temp_password", nullable = false)
    private boolean isTempPassword;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_by")
    private Long createdBy;
}
