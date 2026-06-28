package com.docbridge.docbridge.module.transaction.transaction;

import com.docbridge.docbridge.module.log.audit.AuditAction;
import com.docbridge.docbridge.module.log.audit.AuditLogDocument;
import com.docbridge.docbridge.module.log.audit.AuditLogService;
import com.docbridge.docbridge.module.log.audit.AuditTargetType;
import com.docbridge.docbridge.module.transaction.account.AccountEmailRepository;
import com.docbridge.docbridge.module.transaction.account.AccountEmailView;
import com.docbridge.docbridge.module.transaction.transaction.dto.*;
import com.docbridge.docbridge.module.transaction.transaction_history.TransactionHistoryEntity;
import com.docbridge.docbridge.module.transaction.transaction_history.TransactionHistoryRepository;
import com.docbridge.docbridge.module.transaction.transaction_history.dto.ActorBriefResponse;
import com.docbridge.docbridge.module.transaction.transaction_history.dto.TransactionHistoryResponse;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import com.docbridge.docbridge.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionHistoryRepository historyRepository;
    private final UnitTransactionRepository unitRepository;
    private final SystemTransactionRepository systemRepository;
    private final TransactionCodeGenerator codeGenerator;
    private final AccountEmailRepository accountEmailRepository;
    private final AuditLogService auditLogService;

    // ================================================================
    // UC5.1 — Tạo yêu cầu gửi văn bản
    // ================================================================
    @Transactional
    public TransactionResponse send(SendTransactionRequest request) {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Long senderUnitId = SecurityUtils.getCurrentUnitId(); // throws nếu không phải UNIT

        // 1. Validate sender unit còn active (system + unit)
        UnitTransactionSummary sender = unitRepository.findSummaryById(senderUnitId)
                .orElseThrow(() -> new AppException(ErrorCode.UNIT_NOT_FOUND));
        validateUnitCanTransact(sender);

        // 2. Tìm và validate receiver
        UnitTransactionSummary receiver = unitRepository
                .findSummaryByInteropCode(request.getReceiverInteropCode())
                .orElseThrow(() -> new AppException(ErrorCode.UNIT_NOT_FOUND));

        if (receiver.getId().equals(senderUnitId)) {
            throw new AppException(ErrorCode.CANNOT_SEND_TO_SELF);
        }

        validateReceiverCanReceive(receiver);

        // 3. Sinh mã giao dịch
        String txCode = codeGenerator.next();

        // 4. Lưu transaction
        TransactionEntity tx = TransactionEntity.builder()
                .transactionCode(txCode)
                .senderUnitId(senderUnitId)
                .receiverUnitId(receiver.getId())
                .documentCode(request.getDocumentCode())
                .title(request.getTitle())
                .fileReference(request.getFileReference())
                .note(request.getNote())
                .status(TransactionStatus.SENT)
                .createdBy(accountId)
                .build();
        tx = transactionRepository.save(tx);

        // 5. Ghi history (initial)
        saveHistory(tx.getId(), null, TransactionStatus.SENT, null, accountId);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(accountId)
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.SEND.name())
                .targetType(AuditTargetType.TRANSACTION.name())
                .targetId(String.valueOf(tx.getId()))
                .description("Gửi văn bản '" + tx.getTitle()
                        + "' [" + tx.getTransactionCode() + "]"
                        + " từ " + sender.getInteropCode()
                        + " đến " + receiver.getInteropCode())
                .result("SUCCESS")
                .build());

        return toResponse(tx, sender, receiver, null);
    }

    // ================================================================
    // UC5.2 — Outbox list
    // ================================================================
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getOutbox(TransactionFilterRequest filter) {
        Long unitId = SecurityUtils.getCurrentUnitId();
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize());

        return transactionRepository.findOutbox(
                unitId,
                filter.getKeyword(),
                filter.getCounterpartCode(),
                filter.getStatus(),
                filter.getFrom(),
                filter.getTo(),
                pageable
        ).map(tx -> toResponseWithUnits(tx, null));
    }

    // ================================================================
    // UC5.3 — Chi tiết giao dịch gửi
    // ================================================================
    @Transactional(readOnly = true)
    public TransactionResponse getOutboxDetail(String transactionCode) {
        Long unitId = SecurityUtils.getCurrentUnitId();
        TransactionEntity tx = findAndCheckOwner(transactionCode, unitId, true);
        List<TransactionHistoryResponse> history = buildHistory(tx.getId());
        return toResponseWithUnits(tx, history);
    }

    // ================================================================
    // UC5.4 — Thu hồi
    // ================================================================
    @Transactional
    public void cancel(String transactionCode, CancelTransactionRequest request) {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Long unitId = SecurityUtils.getCurrentUnitId();

        TransactionEntity tx = findAndCheckOwner(transactionCode, unitId, true);

        // Chỉ được cancel khi SENT
        if (tx.getStatus() != TransactionStatus.SENT) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_EDITABLE);
        }

        // Optimistic lock check
        checkVersion(tx, request.getVersion());

        TransactionStatus prev = tx.getStatus();
        tx.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(tx);

        saveHistory(tx.getId(), prev, TransactionStatus.CANCELLED, request.getReason(), accountId);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(accountId)
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.CANCEL.name())
                .targetType(AuditTargetType.TRANSACTION.name())
                .targetId(String.valueOf(tx.getId()))
                .description("Thu hồi giao dịch '" + tx.getTitle()
                        + "' [" + transactionCode + "]"
                        + ", lý do: " + request.getReason())
                .result("SUCCESS")
                .build());
    }

    // ================================================================
    // UC6.1 — Inbox list
    // ================================================================
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getInbox(TransactionFilterRequest filter) {
        Long unitId = SecurityUtils.getCurrentUnitId();
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize());

        return transactionRepository.findInbox(
                unitId,
                filter.getKeyword(),
                filter.getCounterpartCode(),
                filter.getStatus(),
                filter.getFrom(),
                filter.getTo(),
                pageable
        ).map(tx -> toResponseWithUnits(tx, null));
    }

    // ================================================================
    // UC6.2 — Chi tiết giao dịch nhận
    // ================================================================
    @Transactional(readOnly = true)
    public TransactionResponse getInboxDetail(String transactionCode) {
        Long unitId = SecurityUtils.getCurrentUnitId();
        TransactionEntity tx = findAndCheckOwner(transactionCode, unitId, false);
        List<TransactionHistoryResponse> history = buildHistory(tx.getId());
        return toResponseWithUnits(tx, history);
    }

    // ================================================================
    // UC6.3 — Chấp nhận
    // ================================================================
    @Transactional
    public void accept(String transactionCode, AcceptTransactionRequest request) {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Long unitId = SecurityUtils.getCurrentUnitId();

        TransactionEntity tx = findAndCheckOwner(transactionCode, unitId, false);

        if (tx.getStatus() != TransactionStatus.SENT) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_EDITABLE);
        }

        checkVersion(tx, request.getVersion());

        TransactionStatus prev = tx.getStatus();
        tx.setStatus(TransactionStatus.ACCEPTED);
        transactionRepository.save(tx);

        saveHistory(tx.getId(), prev, TransactionStatus.ACCEPTED, null, accountId);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(accountId)
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.ACCEPT.name())
                .targetType(AuditTargetType.TRANSACTION.name())
                .targetId(String.valueOf(tx.getId()))
                .description("Chấp nhận văn bản '" + tx.getTitle() + "' [" + transactionCode + "]")
                .result("SUCCESS")
                .build());
    }

    // ================================================================
    // UC6.4 — Từ chối
    // ================================================================
    @Transactional
    public void reject(String transactionCode, RejectTransactionRequest request) {
        Long accountId = SecurityUtils.getCurrentAccountId();
        Long unitId = SecurityUtils.getCurrentUnitId();

        TransactionEntity tx = findAndCheckOwner(transactionCode, unitId, false);

        if (tx.getStatus() != TransactionStatus.SENT) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_EDITABLE);
        }

        checkVersion(tx, request.getVersion());

        TransactionStatus prev = tx.getStatus();
        tx.setStatus(TransactionStatus.REJECTED);
        transactionRepository.save(tx);

        saveHistory(tx.getId(), prev, TransactionStatus.REJECTED, request.getReason(), accountId);

        auditLogService.log(AuditLogDocument.builder()
                .actorId(accountId)
                .actorEmail(SecurityUtils.getCurrentEmail())
                .actorRole(SecurityUtils.getCurrentRole())
                .action(AuditAction.REJECT_TRANSACTION.name())
                .targetType(AuditTargetType.TRANSACTION.name())
                .targetId(String.valueOf(tx.getId()))
                .description("Từ chối văn bản '" + tx.getTitle()
                        + "' [" + transactionCode + "]"
                        + ", lý do: " + request.getReason())
                .result("SUCCESS")
                .build());
    }

    // ================================================================
    // Private helpers
    // ================================================================

    /**
     * Tìm transaction theo code.
     * isSender=true  → kiểm tra senderUnitId (Outbox)
     * isSender=false → kiểm tra receiverUnitId (Inbox)
     */
    private TransactionEntity findAndCheckOwner(String code, Long unitId, boolean isSender) {
        TransactionEntity tx = transactionRepository.findByTransactionCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        boolean owns = isSender
                ? tx.getSenderUnitId().equals(unitId)
                : tx.getReceiverUnitId().equals(unitId);

        if (!owns) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND); // không lộ tồn tại
        }
        return tx;
    }

    private void checkVersion(TransactionEntity tx, Integer requestVersion) {
        if (!tx.getVersion().equals(requestVersion)) {
            throw new AppException(ErrorCode.TRANSACTION_OPTIMISTIC_LOCK);
        }
    }

    /**
     * Validate sender unit: system không bị khoá, unit không bị khoá, unit ACTIVE.
     */
    private void validateUnitCanTransact(UnitTransactionSummary unit) {
        String systemStatus = systemRepository.findStatusById(unit.getSystemId());
        if ("LOCKED".equals(systemStatus)) {
            throw new AppException(ErrorCode.SYSTEM_LOCKED);
        }
        if ("LOCKED".equals(unit.getStatus())) {
            throw new AppException(ErrorCode.UNIT_LOCKED);
        }
        if (!"ACTIVE".equals(unit.getStatus())) {
            throw new AppException(ErrorCode.UNIT_NOT_ACTIVE);
        }
    }

    /**
     * Validate receiver: system → unit theo thứ tự.
     * PENDING / REJECTED trả NOT_FOUND (không lộ thông tin).
     */
    private void validateReceiverCanReceive(UnitTransactionSummary unit) {
        String systemStatus = systemRepository.findStatusById(unit.getSystemId());
        if ("LOCKED".equals(systemStatus)) {
            throw new AppException(ErrorCode.RECEIVER_SYSTEM_LOCKED);
        }
        if ("LOCKED".equals(unit.getStatus())) {
            throw new AppException(ErrorCode.RECEIVER_UNIT_LOCKED);
        }
        if (!"ACTIVE".equals(unit.getStatus())) {
            // PENDING hoặc REJECTED — không lộ
            throw new AppException(ErrorCode.UNIT_NOT_FOUND);
        }
    }

    private void saveHistory(Long txId, TransactionStatus from, TransactionStatus to,
                             String reason, Long actedBy) {
        historyRepository.save(TransactionHistoryEntity.builder()
                .transactionId(txId)
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .actedBy(actedBy)
                .actedAt(LocalDateTime.now())
                .build());
    }

    private List<TransactionHistoryResponse> buildHistory(Long txId) {
        List<TransactionHistoryEntity> entries =
                historyRepository.findByTransactionIdOrderByActedAtAsc(txId);

        Set<Long> actorIds = entries.stream()
                .map(TransactionHistoryEntity::getActedBy)
                .collect(Collectors.toSet());

        Map<Long, String> emailById = accountEmailRepository.findEmailsByIds(actorIds)
                .stream()
                .collect(Collectors.toMap(AccountEmailView::getId, AccountEmailView::getEmail));

        return entries.stream()
                .map(h -> TransactionHistoryResponse.builder()
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .reason(h.getReason())
                        .actedBy(ActorBriefResponse.builder()
                                .id(h.getActedBy())
                                .email(emailById.getOrDefault(h.getActedBy(), "–"))
                                .build())
                        .actedAt(h.getActedAt())
                        .build())
                .toList();
    }

    /**
     * Map entity → response, tự load unit nếu chưa có.
     */
    private TransactionResponse toResponseWithUnits(TransactionEntity tx,
                                                    List<TransactionHistoryResponse> history) {
        UnitTransactionSummary sender = unitRepository.findSummaryById(tx.getSenderUnitId())
                .orElse(null);
        UnitTransactionSummary receiver = unitRepository.findSummaryById(tx.getReceiverUnitId())
                .orElse(null);
        return toResponse(tx, sender, receiver, history);
    }

    private TransactionResponse toResponse(TransactionEntity tx,
                                           UnitTransactionSummary sender,
                                           UnitTransactionSummary receiver,
                                           List<TransactionHistoryResponse> history) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .transactionCode(tx.getTransactionCode())
                .sender(sender == null ? null : UnitBriefResponse.builder()
                        .id(sender.getId())
                        .interopCode(sender.getInteropCode())
                        .name(sender.getName())
                        .build())
                .receiver(receiver == null ? null : UnitBriefResponse.builder()
                        .id(receiver.getId())
                        .interopCode(receiver.getInteropCode())
                        .name(receiver.getName())
                        .build())
                .documentCode(tx.getDocumentCode())
                .title(tx.getTitle())
                .fileReference(tx.getFileReference())
                .note(tx.getNote())
                .status(tx.getStatus())
                .version(tx.getVersion())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .history(history)
                .build();
    }
}
