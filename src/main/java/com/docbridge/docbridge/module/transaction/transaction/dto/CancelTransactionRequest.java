package com.docbridge.docbridge.module.transaction.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelTransactionRequest {

    @NotNull(message = "Version không được để trống")
    private Integer version;

    @NotBlank(message = "Lý do thu hồi không được để trống")
    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String reason;
}
