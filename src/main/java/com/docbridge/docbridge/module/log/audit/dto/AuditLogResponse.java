package com.docbridge.docbridge.module.log.audit.dto;

import com.docbridge.docbridge.module.log.audit.AuditLogDocument;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AuditLogResponse {
    private String id;
    private Long actorId;
    private String actorEmail;
    private String actorRole;
    private String action;
    private String targetType;
    private String targetId;
    private String description;
    private String ipAddress;
    private String result;
    private String failureReason;
    private Instant createdAt;

    public static AuditLogResponse from(AuditLogDocument doc) {
        return AuditLogResponse.builder()
                .id(doc.getId())
                .actorId(doc.getActorId())
                .actorEmail(doc.getActorEmail())
                .actorRole(doc.getActorRole())
                .action(doc.getAction())
                .targetType(doc.getTargetType())
                .targetId(doc.getTargetId())
                .description(doc.getDescription())
                .ipAddress(doc.getIpAddress())
                .result(doc.getResult())
                .failureReason(doc.getFailureReason())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
