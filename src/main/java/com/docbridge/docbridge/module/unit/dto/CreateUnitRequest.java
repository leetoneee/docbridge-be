package com.docbridge.docbridge.module.unit.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

// ── UC2.1 ──────────────────────────────────────────────────────────────────
@Getter
@Setter
public class CreateUnitRequest {

    @NotNull(message = "Hệ thống liên thông không được để trống")
    private Long systemId;

    @NotBlank(message = "Tên đơn vị không được để trống")
    @Size(max = 255)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank(message = "Tên người đại diện không được để trống")
    @Size(max = 255)
    private String representativeName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 20)
    private String representativePhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255)
    private String email;
}
