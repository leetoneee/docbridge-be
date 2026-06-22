package com.docbridge.docbridge.module.transaction.transaction;

import com.docbridge.docbridge.module.transaction.transaction.dto.*;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Gửi và nhận văn bản liên thông")
public class TransactionController {

    private final TransactionService transactionService;

    // ================================================================
    // OUTBOX — UC5.*
    // ================================================================

    // ----------------------------------------------------------------
    // UC5.1 — Tạo yêu cầu gửi văn bản
    // POST /api/transactions/outbox
    // ----------------------------------------------------------------
    @PostMapping("/outbox")
    @PreAuthorize("hasAuthority('DOCUMENT_SEND')")
    @Operation(
        summary = "Tạo yêu cầu gửi văn bản",
        description = "Unit gửi văn bản đến đơn vị khác. Trả về giao dịch vừa tạo với status=SENT"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> send(
            @Valid @RequestBody SendTransactionRequest request) {

        TransactionResponse response = transactionService.send(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi văn bản thành công", response));
    }

    // ----------------------------------------------------------------
    // UC5.2 + UC5.5 — Danh sách / Tìm kiếm Outbox
    // GET /api/transactions/outbox
    // ----------------------------------------------------------------
    @GetMapping("/outbox")
    @PreAuthorize("hasAuthority('DOCUMENT_SEND')")
    @Operation(
        summary = "Danh sách văn bản đã gửi (Outbox)",
        description = "Hỗ trợ filter: documentCode, title, counterpartCode (mã đơn vị nhận), status, from, to. " +
                      "Mặc định sort theo createdAt DESC"
    )
    public ResponseEntity<ApiResponse<ApiResponse.PageData<TransactionResponse>>> getOutbox(
            @ModelAttribute TransactionFilterRequest filter) {

        Page<TransactionResponse> page = transactionService.getOutbox(filter);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // ----------------------------------------------------------------
    // UC5.3 — Chi tiết giao dịch gửi (kèm history)
    // GET /api/transactions/outbox/{transactionCode}
    // ----------------------------------------------------------------
    @GetMapping("/outbox/{transactionCode}")
    @PreAuthorize("hasAuthority('DOCUMENT_SEND')")
    @Operation(
        summary = "Chi tiết giao dịch gửi",
        description = "Trả về đầy đủ metadata và lịch sử thay đổi trạng thái"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> getOutboxDetail(
            @PathVariable String transactionCode) {

        return ResponseEntity.ok(
                ApiResponse.success(transactionService.getOutboxDetail(transactionCode)));
    }

    // ----------------------------------------------------------------
    // UC5.4 — Thu hồi
    // PATCH /api/transactions/outbox/{transactionCode}/cancel
    // ----------------------------------------------------------------
    @PatchMapping("/outbox/{transactionCode}/cancel")
    @PreAuthorize("hasAuthority('DOCUMENT_CANCEL')")
    @Operation(
        summary = "Thu hồi văn bản",
        description = "Chỉ thực hiện được khi status=SENT. Bắt buộc nhập lý do. " +
                      "Request body phải chứa version hiện tại để tránh xung đột"
    )
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable String transactionCode,
            @Valid @RequestBody CancelTransactionRequest request) {

        transactionService.cancel(transactionCode, request);
        return ResponseEntity.ok(ApiResponse.success("Thu hồi văn bản thành công"));
    }

    // ================================================================
    // INBOX — UC6.*
    // ================================================================

    // ----------------------------------------------------------------
    // UC6.1 + UC6.5 — Danh sách / Tìm kiếm Inbox
    // GET /api/transactions/inbox
    // ----------------------------------------------------------------
    @GetMapping("/inbox")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW_INBOX')")
    @Operation(
        summary = "Danh sách văn bản nhận được (Inbox)",
        description = "Hỗ trợ filter: documentCode, title, counterpartCode (mã đơn vị gửi), status, from, to. " +
                      "Mặc định sort theo createdAt DESC"
    )
    public ResponseEntity<ApiResponse<ApiResponse.PageData<TransactionResponse>>> getInbox(
            @ModelAttribute TransactionFilterRequest filter) {

        Page<TransactionResponse> page = transactionService.getInbox(filter);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // ----------------------------------------------------------------
    // UC6.2 — Chi tiết giao dịch nhận (kèm history)
    // GET /api/transactions/inbox/{transactionCode}
    // ----------------------------------------------------------------
    @GetMapping("/inbox/{transactionCode}")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW_INBOX')")
    @Operation(
        summary = "Chi tiết giao dịch nhận",
        description = "Trả về đầy đủ metadata, thông tin đơn vị gửi, file reference và lịch sử"
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> getInboxDetail(
            @PathVariable String transactionCode) {

        return ResponseEntity.ok(
                ApiResponse.success(transactionService.getInboxDetail(transactionCode)));
    }

    // ----------------------------------------------------------------
    // UC6.3 — Chấp nhận văn bản
    // PATCH /api/transactions/inbox/{transactionCode}/accept
    // ----------------------------------------------------------------
    @PatchMapping("/inbox/{transactionCode}/accept")
    @PreAuthorize("hasAuthority('DOCUMENT_ACCEPT')")
    @Operation(
        summary = "Chấp nhận văn bản",
        description = "Chuyển status sang ACCEPTED. Chỉ thực hiện được khi status=SENT. " +
                      "Request body phải chứa version hiện tại"
    )
    public ResponseEntity<ApiResponse<Void>> accept(
            @PathVariable String transactionCode,
            @Valid @RequestBody AcceptTransactionRequest request) {

        transactionService.accept(transactionCode, request);
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận văn bản thành công"));
    }

    // ----------------------------------------------------------------
    // UC6.4 — Từ chối văn bản
    // PATCH /api/transactions/inbox/{transactionCode}/reject
    // ----------------------------------------------------------------
    @PatchMapping("/inbox/{transactionCode}/reject")
    @PreAuthorize("hasAuthority('DOCUMENT_REJECT')")
    @Operation(
        summary = "Từ chối văn bản",
        description = "Chuyển status sang REJECTED. Chỉ thực hiện được khi status=SENT. " +
                      "Bắt buộc nhập lý do. Request body phải chứa version hiện tại"
    )
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable String transactionCode,
            @Valid @RequestBody RejectTransactionRequest request) {

        transactionService.reject(transactionCode, request);
        return ResponseEntity.ok(ApiResponse.success("Từ chối văn bản thành công"));
    }
}
