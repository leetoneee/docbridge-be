package com.docbridge.docbridge.module.unit.dto;

import com.docbridge.docbridge.module.unit.InteropUnitStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// ── Detail response (UC2.3) ─────────────────────────────────────────────────
@Getter
@Builder
public class UnitDetailResponse {

    private Long id;
    private String interopCode;
    private String name;
    private String description;
    private String email;
    private String representativeName;
    private String representativePhone;
    private InteropUnitStatus status;
    private String rejectedReason;

    private UnitResponse.SystemSummary system;

    // Thông tin tài khoản Unit đại diện (null nếu chưa phê duyệt)
    private UnitAccountSummary unitAccount;

    private String approvedBy;      // email của admin phê duyệt
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class UnitAccountSummary {
        private String email;
        private String status;
        private LocalDateTime lastLoginAt;
    }
}
