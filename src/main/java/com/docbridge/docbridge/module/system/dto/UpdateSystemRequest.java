package com.docbridge.docbridge.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSystemRequest {

    // code không cho cập nhật — chỉ name và description
    @NotBlank(message = "Tên hệ thống không được để trống")
    @Size(max = 255, message = "Tên hệ thống tối đa 255 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}
