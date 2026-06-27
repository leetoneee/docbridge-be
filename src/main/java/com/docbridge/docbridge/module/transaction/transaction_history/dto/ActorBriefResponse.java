package com.docbridge.docbridge.module.transaction.transaction_history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ActorBriefResponse {
    private Long id;
    private String email;
}
