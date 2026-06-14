package com.docbridge.docbridge.module.unit.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

// ── UC2.4 — chỉ cho sửa name, description, representative_name/phone ──────
@Getter
@Setter
public class UpdateUnitRequest {

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
}
