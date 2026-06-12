package com.docbridge.docbridge.module.permission.role.dto;

import com.docbridge.docbridge.module.permission.permission.dto.PermissionResponse;
import com.docbridge.docbridge.module.permission.role.RoleEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class RoleDetailResponse {

    private Long id;
    private String code;
    private String name;
    private String description;

    // permissions nhóm theo groupName để frontend render dễ
    private Map<String, List<PermissionResponse>> permissionsByGroup;

    public static RoleDetailResponse from(RoleEntity entity) {
        Map<String, List<PermissionResponse>> grouped = entity.getRolePermissions().stream()
                .map(rp -> PermissionResponse.from(rp.getPermission()))
                .collect(Collectors.groupingBy(
                        PermissionResponse::getGroupName,
                        Collectors.toList()
                ));

        return RoleDetailResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .permissionsByGroup(grouped)
                .build();
    }
}
