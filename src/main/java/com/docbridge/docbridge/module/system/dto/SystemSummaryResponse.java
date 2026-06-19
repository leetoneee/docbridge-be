package com.docbridge.docbridge.module.system.dto;

import com.docbridge.docbridge.module.system.InteropSystemEntity;
import com.docbridge.docbridge.module.system.InteropSystemStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemSummaryResponse {
    private Long id;
    private String code;
    private String name;
    private InteropSystemStatus status;

    public static SystemSummaryResponse from(InteropSystemEntity entity) {
        return SystemSummaryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .status(entity.getStatus())
                .build();
    }
}
