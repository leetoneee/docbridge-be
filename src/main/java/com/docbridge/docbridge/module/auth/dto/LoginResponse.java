package com.docbridge.docbridge.module.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String  token;
    private String  email;
    private String  role;

    /**
     * true  → client phải redirect đến trang đổi mật khẩu lần đầu
     * false → vào thẳng dashboard
     */
    private boolean mustChangePassword;
}
