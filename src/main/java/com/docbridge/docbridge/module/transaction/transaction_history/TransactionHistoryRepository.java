package com.docbridge.docbridge.module.transaction.transaction_history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, Long> {

    List<TransactionHistoryEntity> findByTransactionIdOrderByActedAtAsc(Long transactionId);
}
