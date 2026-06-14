package com.docbridge.docbridge.module.unit;

import com.docbridge.docbridge.module.system.InteropSystemEntity;
import com.docbridge.docbridge.shared.kernel.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interop_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteropUnitEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id", nullable = false)
    private InteropSystemEntity system;

    // Nullable — sinh khi phê duyệt (UC2.5)
    @Column(name = "interop_code", unique = true)
    private String interopCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "representative_name", nullable = false)
    private String representativeName;

    @Column(name = "representative_phone", nullable = false)
    private String representativePhone;

    // Dùng làm username của account Unit — UNIQUE
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InteropUnitStatus status;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_by")
    private Long createdBy;
}
