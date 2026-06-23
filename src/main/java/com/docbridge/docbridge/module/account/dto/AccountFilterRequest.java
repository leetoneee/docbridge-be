package com.docbridge.docbridge.module.account.dto;

import com.docbridge.docbridge.module.account.AccountStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountFilterRequest {

    /** Tìm theo email, LIKE %email%. Null = không lọc. */
    private String email;

    /** Code của role: ADMIN, OPERATOR, UNIT. Null = không lọc. */
    private String role;

    /** Trạng thái tài khoản. Null = không lọc. */
    private AccountStatus status;

    private int page = 0;
    private int size = 20;
}
