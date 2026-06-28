package com.docbridge.docbridge.module.log.audit.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class AuditLogFilterRequest {
    private Instant dateFrom;
    private Instant dateTo;
    private List<String> actions;   // multi-select
    private String actorEmail;
    private String targetType;      // ACCOUNT / INTEROP_UNIT / ...
    private String targetId;
    private String result;          // SUCCESS / FAILURE
    private String searchAfter;     // cursor từ response trước
    private Integer size;
}
