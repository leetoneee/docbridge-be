package com.docbridge.docbridge.module.permission.permission.dto;

import com.docbridge.docbridge.module.permission.permission.PermissionEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String groupName;
    private boolean assigned;

    public static PermissionResponse from(PermissionEntity entity) {
        return from(entity, false);
    }

    public static PermissionResponse from(PermissionEntity entity, boolean assigned) {
        return PermissionResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .groupName(entity.getGroupName())
                .assigned(assigned)
                .build();
    }
}
