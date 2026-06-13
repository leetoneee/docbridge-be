package com.docbridge.docbridge.module.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Trả về ngay sau khi tạo tài khoản hoặc reset password.
 * tempPassword là plaintext, chỉ xuất hiện 1 lần trong response này.
 * Đồng thời hệ thống gửi email chứa tempPassword đến địa chỉ email tài khoản.
 */
@Getter
@AllArgsConstructor
public class CreateAccountResult {

    private Long accountId;
    private String email;

    /**
     * Mật khẩu tạm thời — plaintext, hiển thị 1 lần.
     */
    private String tempPassword;
}
