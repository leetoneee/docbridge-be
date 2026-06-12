package com.docbridge.docbridge.module.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Load account từ DB theo email, trả về AccountPrincipal.
 * Được Spring Security gọi khi xác thực username/password
 * và được JwtAuthFilter gọi để dựng lại Principal từ token.
 */
@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountAuthRepository accountAuthRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        AccountAuthRepository.AccountAuthProjection acc =
                accountAuthRepository.findAuthProjectionByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "Account not found: " + email));

        List<String> permissions =
                accountAuthRepository.findPermissionCodesByRoleId(acc.getRoleId());

        return new AccountPrincipal(
                acc.getId(),
                acc.getEmail(),
                acc.getPassword(),
                acc.getRoleCode(),
                acc.getUnitId(),
                acc.getIsTempPassword(),
                "ACTIVE".equals(acc.getStatus()),
                permissions
        );
    }
}
