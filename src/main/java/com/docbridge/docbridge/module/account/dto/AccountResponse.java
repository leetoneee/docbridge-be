package com.docbridge.docbridge.module.account.dto;

import com.docbridge.docbridge.module.account.AccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccountResponse {

    private Long id;
    private String email;
    private String roleCode;
    private String roleName;
    private AccountStatus status;
    private boolean isTempPassword;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    /**
     * Chỉ có giá trị với tài khoản UNIT.
     */
    private UnitInfo unitInfo;

    @Getter
    @Builder
    public static class UnitInfo {
        private Long unitId;
        private String interopCode;
        private String unitName;
    }
}
