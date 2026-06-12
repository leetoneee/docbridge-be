package com.docbridge.docbridge.module.permission.role.dto;

import com.docbridge.docbridge.module.permission.role.RoleEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class RoleResponse {

    private Long id;
    private String code;
    private String name;
    private String description;

    // UC8.1: list — chỉ trả tổng số permission, không trả chi tiết
    private int permissionCount;

    public static RoleResponse from(RoleEntity entity) {
        return RoleResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .permissionCount(entity.getRolePermissions().size())
                .build();
    }
}
