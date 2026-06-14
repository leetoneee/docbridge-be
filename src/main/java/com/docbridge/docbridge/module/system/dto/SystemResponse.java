package com.docbridge.docbridge.module.system.dto;

import com.docbridge.docbridge.module.system.InteropSystemEntity;
import com.docbridge.docbridge.module.system.InteropSystemStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SystemResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private InteropSystemStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SystemResponse from(InteropSystemEntity entity) {
        return SystemResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

