package com.docbridge.docbridge.config;

import com.docbridge.docbridge.module.log.audit.AuditLogRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public AuditLogRepository auditLogRepository() {
        return mock(AuditLogRepository.class);
    }
}
