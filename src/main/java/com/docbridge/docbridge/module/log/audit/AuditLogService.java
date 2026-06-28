package com.docbridge.docbridge.module.log.audit;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.docbridge.docbridge.module.log.audit.dto.AuditLogFilterRequest;
import com.docbridge.docbridge.module.log.audit.dto.AuditLogResponse;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ElasticsearchClient esClient;

    // ----------------------------------------------------------------
    // Ghi log — @Async, không throw ra ngoài
    // ----------------------------------------------------------------
    @Async
    public void log(AuditLogDocument entry) {
        try {
            entry.setId(UUID.randomUUID().toString());
            entry.setCreatedAt(Instant.now());
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: action={}, actor={}",
                    entry.getAction(), entry.getActorEmail(), e);
        }
    }

    // ----------------------------------------------------------------
    // UC7.1 / UC7.2 — Danh sách + filter, search_after pagination
    // ----------------------------------------------------------------
    public Map<String, Object> search(AuditLogFilterRequest filter) {
        try {
            List<Query> musts = new ArrayList<>();

            if (filter.getDateFrom() != null) {
                musts.add(Query.of(q -> q.range(r -> r.date(d -> d
                        .field("created_at")
                        .gte(filter.getDateFrom().toString())
                ))));
            }
            if (filter.getDateTo() != null) {
                musts.add(Query.of(q -> q.range(r -> r.date(d -> d
                        .field("created_at")
                        .lte(filter.getDateTo().toString())
                ))));
            }
            if (filter.getActions() != null && !filter.getActions().isEmpty()) {
                musts.add(Query.of(q -> q.terms(t -> t
                        .field("action")
                        .terms(tv -> tv.value(filter.getActions().stream()
                                .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                                .toList())))));
            }
            if (filter.getActorEmail() != null) {
                musts.add(Query.of(q -> q.term(t -> t
                        .field("actor_email")
                        .value(filter.getActorEmail()))));
            }
            if (filter.getTargetType() != null) {
                musts.add(Query.of(q -> q.term(t -> t
                        .field("target_type")
                        .value(filter.getTargetType()))));
            }
            if (filter.getTargetId() != null) {
                musts.add(Query.of(q -> q.term(t -> t
                        .field("target_id")
                        .value(filter.getTargetId()))));
            }
            if (filter.getResult() != null) {
                musts.add(Query.of(q -> q.term(t -> t
                        .field("result")
                        .value(filter.getResult()))));
            }

            Query finalQuery = musts.isEmpty()
                    ? Query.of(q -> q.matchAll(m -> m))
                    : Query.of(q -> q.bool(b -> b.must(musts)));

            int size = filter.getSize() != null ? filter.getSize() : 20;

            SearchRequest.Builder builder = new SearchRequest.Builder()
                    .index("audit_log")
                    .query(finalQuery)
                    .sort(s -> s.field(f -> f
                            .field("created_at").order(SortOrder.Desc)))
                    .size(size);

            // search_after cursor
            if (filter.getSearchAfter() != null) {
                builder.searchAfter(co.elastic.clients.elasticsearch._types.FieldValue
                        .of(filter.getSearchAfter()));
            }

            SearchResponse<AuditLogDocument> resp =
                    esClient.search(builder.build(), AuditLogDocument.class);

            List<AuditLogResponse> items = resp.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(AuditLogResponse::from)
                    .toList();

            // Lấy sort value của record cuối để làm cursor trang sau
            String nextCursor = null;
            List<Hit<AuditLogDocument>> hits = resp.hits().hits();
            if (!hits.isEmpty()) {
                List<co.elastic.clients.elasticsearch._types.FieldValue> sortVals =
                        hits.getLast().sort();
                if (!sortVals.isEmpty()) {
                    nextCursor = sortVals.getFirst().toString();
                }
            }

            return Map.of(
                    "items", items,
                    "nextCursor", nextCursor != null ? nextCursor : "",
                    "hasMore", items.size() == size
            );
        } catch (Exception e) {
            log.error("Failed to search audit log", e);
            return Map.of("items", List.of(), "nextCursor", "", "hasMore", false);
        }
    }

    // ----------------------------------------------------------------
    // UC7.3 — Chi tiết log
    // ----------------------------------------------------------------
    public AuditLogResponse getById(String id) {
        return auditLogRepository.findById(id)
                .map(AuditLogResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.LOG_NOT_FOUND));
    }
}
