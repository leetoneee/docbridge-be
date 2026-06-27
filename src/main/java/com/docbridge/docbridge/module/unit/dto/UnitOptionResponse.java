package com.docbridge.docbridge.module.unit.dto;

import com.docbridge.docbridge.module.unit.InteropUnitEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnitOptionResponse {
    private Long id;
    private String interopCode;
    private String name;
    private String email;

    public static UnitOptionResponse from(InteropUnitEntity u) {
        return UnitOptionResponse.builder()
                .id(u.getId())
                .interopCode(u.getInteropCode())
                .name(u.getName())
                .email(u.getEmail())
                .build();
    }
}
