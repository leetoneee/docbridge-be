package com.docbridge.docbridge.module.unit.dto;

import com.docbridge.docbridge.module.unit.InteropUnitStatus;
import lombok.Getter;
import lombok.Setter;

// ── UC2.2 / UC2.8 ──────────────────────────────────────────────────────────
@Getter
@Setter
public class UnitFilterRequest {

    private Long systemId;
    private InteropUnitStatus status;
    private String keyword;   // tìm theo name hoặc interopCode
    private int page = 0;
    private int size = 20;
}
