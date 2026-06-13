package com.docbridge.docbridge.module.account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Read-only projection của bảng interop_unit.
 * Module account chỉ cần tên đơn vị + mã liên thông để hiển thị trong AccountResponse.
 * Không import InteropUnitEntity từ module unit để tránh coupling.
 */
@Entity
@Table(name = "interop_unit")
@Getter
@NoArgsConstructor
@Immutable
public class InteropUnitSummary {

    @Id
    private Long id;

    @Column(name = "interop_code")
    private String interopCode;

    @Column(name = "name")
    private String name;
}
