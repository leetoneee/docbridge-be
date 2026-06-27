package com.docbridge.docbridge.module.transaction.transaction.dto;

import com.docbridge.docbridge.module.transaction.transaction.TransactionStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionFilterRequest {

    private String keyword;

    /** Outbox: mã liên thông đơn vị nhận / Inbox: mã liên thông đơn vị gửi */
    private String counterpartCode;

    private TransactionStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    private int page = 0;
    private int size = 20;
}
