package com.docbridge.docbridge.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSystemRequest {

    /**
     * Mã hệ thống: viết hoa, không dấu, không khoảng trắng, tối đa 50 ký tự.
     * VD: EOFFICE, IOFFICE
     */
    @NotBlank(message = "Mã hệ thống không được để trống")
    @Pattern(
            regexp = "^[A-Z0-9_]+$",
            message = "Mã hệ thống chỉ được chứa chữ hoa, số và dấu gạch dưới"
    )
    @Size(max = 50, message = "Mã hệ thống tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Tên hệ thống không được để trống")
    @Size(max = 255, message = "Tên hệ thống tối đa 255 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}