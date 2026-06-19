package com.docbridge.docbridge.module.unit.dto;

// ── UC2.4 (Admin only) — Đổi email đơn vị ──────────────────────────────────

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUnitEmailRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255)
    private String email;
}
