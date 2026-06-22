package com.docbridge.docbridge.module.transaction.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemTransactionRepository
        extends JpaRepository<com.docbridge.docbridge.module.system.InteropSystemEntity, Long> {

    @Query("SELECT s.status FROM InteropSystemEntity s WHERE s.id = :id")
    String findStatusById(@Param("id") Long id);
}
  