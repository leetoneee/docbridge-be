package com.docbridge.docbridge.module.unit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// ── UC2.5 — Từ chối ────────────────────────────────────────────────────────
@Getter
@Setter
public class RejectUnitRequest {

    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(max = 500)
    private String reason;
}
