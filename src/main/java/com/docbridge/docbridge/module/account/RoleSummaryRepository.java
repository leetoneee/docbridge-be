package com.docbridge.docbridge.module.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleSummaryRepository extends JpaRepository<RoleSummary, Long> {

    Optional<RoleSummary> findByCode(String code);
}
