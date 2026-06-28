package com.docbridge.docbridge.module.log.audit;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;

@Document(indexName = "audit_log")
@Mapping(mappingPath = "elasticsearch/audit-log-mapping.json")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long, name = "actor_id")
    private Long actorId;

    @Field(type = FieldType.Keyword, name = "actor_email")
    private String actorEmail;

    @Field(type = FieldType.Keyword, name = "actor_role")
    private String actorRole;

    @Field(type = FieldType.Keyword)
    private String action;

    @Field(type = FieldType.Keyword, name = "target_type")
    private String targetType;

    @Field(type = FieldType.Keyword, name = "target_id")
    private String targetId;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword, name = "ip_address")
    private String ipAddress;

    @Field(type = FieldType.Keyword)
    private String result;  // SUCCESS / FAILURE

    @Field(type = FieldType.Text, name = "failure_reason")
    private String failureReason;

    @Field(type = FieldType.Date, name = "created_at",
            format = DateFormat.date_time)
    private Instant createdAt;
}
