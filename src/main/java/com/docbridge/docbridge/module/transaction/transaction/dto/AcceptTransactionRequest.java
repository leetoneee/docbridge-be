package com.docbridge.docbridge.module.transaction.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptTransactionRequest {

    @NotNull(message = "Version không được để trống")
    private Integer version;
}
