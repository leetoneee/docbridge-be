package com.docbridge.docbridge.module.transaction.account;

import com.docbridge.docbridge.module.account.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AccountEmailRepository extends JpaRepository<AccountEntity, Long> {
    @Query("SELECT a.id AS id, a.email AS email FROM AccountEntity a WHERE a.id IN :ids")
    List<AccountEmailView> findEmailsByIds(@Param("ids") Set<Long> ids);
}
