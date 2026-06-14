package com.docbridge.docbridge.module.unit.dto;

import com.docbridge.docbridge.module.unit.InteropUnitEntity;
import com.docbridge.docbridge.module.unit.InteropUnitStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// ── List response (UC2.2) ───────────────────────────────────────────────────
@Getter
@Builder
public class UnitResponse {

    private Long id;
    private String interopCode;
    private String name;
    private String email;
    private InteropUnitStatus status;
    private SystemSummary system;
    private LocalDateTime createdAt;

    public static UnitResponse from(InteropUnitEntity u) {
        return UnitResponse.builder()
                .id(u.getId())
                .interopCode(u.getInteropCode())
                .name(u.getName())
                .email(u.getEmail())
                .status(u.getStatus())
                .system(SystemSummary.builder()
                        .id(u.getSystem().getId())
                        .code(u.getSystem().getCode())
                        .name(u.getSystem().getName())
                        .build())
                .createdAt(u.getCreatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class SystemSummary {
        private Long id;
        private String code;
        private String name;
    }
}
