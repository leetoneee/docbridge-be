package com.docbridge.docbridge.module.account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Read-only projection của bảng role.
 * Module account dùng để trả role code/name trong response, không import RoleEntity từ module permission.
 */
@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor
@org.hibernate.annotations.Immutable
public class RoleSummary {

    @Id
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;
}
