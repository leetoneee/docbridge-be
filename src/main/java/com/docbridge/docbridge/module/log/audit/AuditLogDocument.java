package com.docbridge.docbridge.module.log.audit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogDocument {

    @Id
    private String id;

    @JsonProperty("actor_id")
    @Field(type = FieldType.Long, name = "actor_id")
    private Long actorId;

    @JsonProperty("actor_email")
    @Field(type = FieldType.Keyword, name = "actor_email")
    private String actorEmail;

    @JsonProperty("actor_role")
    @Field(type = FieldType.Keyword, name = "actor_role")
    private String actorRole;

    @Field(type = FieldType.Keyword)
    private String action;

    @JsonProperty("target_type")
    @Field(type = FieldType.Keyword, name = "target_type")
    private String targetType;

    @JsonProperty("target_id")
    @Field(type = FieldType.Keyword, name = "target_id")
    private String targetId;

    @Field(type = FieldType.Text)
    private String description;

    @JsonProperty("ip_address")
    @Field(type = FieldType.Keyword, name = "ip_address")
    private String ipAddress;

    @Field(type = FieldType.Keyword)
    private String result;

    @JsonProperty("failure_reason")
    @Field(type = FieldType.Text, name = "failure_reason")
    private String failureReason;

    @JsonProperty("created_at")
    @Field(type = FieldType.Date, name = "created_at", format = DateFormat.date_time)
    private Instant createdAt;
}
