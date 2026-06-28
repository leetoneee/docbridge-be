package com.docbridge.docbridge.module.auth;

import com.docbridge.docbridge.shared.security.SecurityUtils;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal của DocBridge — đại diện cho tài khoản đang đăng nhập.
 * Được load bởi AccountUserDetailsService, gắn vào SecurityContext qua JwtAuthFilter.
 *
 * Implement SecurityUtils.AccountPrincipalHolder để shared/kernel
 * có thể lấy accountId mà không cần depend vào module auth.
 */
@Getter
public class AccountPrincipal implements UserDetails, SecurityUtils.AccountPrincipalHolder {

    private final Long              accountId;
    private final String            email;
    private final String            password;
    private final String            roleCode;
    private final Long              unitId;       // null nếu ADMIN hoặc OPERATOR
    private final boolean           isTempPassword;
    private final boolean           isActive;
    private final List<String>      permissions;  // VD: ["SYSTEM_VIEW", "UNIT_CREATE"]

    public AccountPrincipal(Long accountId,
                            String email,
                            String password,
                            String roleCode,
                            Long unitId,
                            boolean isTempPassword,
                            boolean isActive,
                            List<String> permissions) {
        this.accountId      = accountId;
        this.email          = email;
        this.password       = password;
        this.roleCode       = roleCode;
        this.unitId         = unitId;
        this.isTempPassword = isTempPassword;
        this.isActive       = isActive;
        this.permissions    = permissions;
    }

    // ----------------------------------------------------------------
    // UserDetails
    // ----------------------------------------------------------------

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Authorities gồm:
     * - ROLE_{roleCode}         → dùng cho hasRole() check
     * - Từng permission code    → dùng cho @PreAuthorize("hasAuthority('UNIT_CREATE')")
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new java.util.ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        return authorities;
    }

    @Override
    public Long getAccountId() { return this.accountId; }

    @Override
    public Long getUnitId() { return this.unitId; }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return isActive; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return isActive; }
}
