package com.docbridge.docbridge.module.transaction.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTransactionRequest {

    @NotBlank(message = "Số hiệu văn bản không được để trống")
    @Size(max = 100, message = "Số hiệu văn bản tối đa 100 ký tự")
    private String documentCode;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500, message = "Tiêu đề tối đa 500 ký tự")
    private String title;

    @NotBlank(message = "Mã liên thông đơn vị nhận không được để trống")
    private String receiverInteropCode;

    @NotBlank(message = "File reference không được để trống")
    @Size(max = 1000, message = "File reference tối đa 1000 ký tự")
    private String fileReference;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
