package com.docbridge.docbridge.module.transaction.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UnitBriefResponse {
    private Long   id;
    private String interopCode;
    private String name;
}
