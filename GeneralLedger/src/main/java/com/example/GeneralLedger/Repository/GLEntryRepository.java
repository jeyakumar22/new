package com.example.GeneralLedger.Repository;

import com.example.GeneralLedger.Model.Account;
import com.example.GeneralLedger.Model.GeneralLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface GLEntryRepository  extends JpaRepository<GeneralLedgerEntry,Long> {
    List<GeneralLedgerEntry> findByAccountOrderByEntryDateAsc(Account account);

    @Query("SELECT COALESCE(SUM(g.debit), 0) FROM GeneralLedgerEntry g WHERE g.account = :account")
    BigDecimal sumDebitByAccount(Account account);

    @Query("SELECT COALESCE(SUM(g.credit), 0) FROM GeneralLedgerEntry g WHERE g.account = :account")
    BigDecimal sumCreditByAccount(Account account);

    @Query("""
        SELECT g.runningBalance 
        FROM GeneralLedgerEntry g 
        WHERE g.account = :account 
        ORDER BY g.entryDate DESC, g.id DESC
    """)
    BigDecimal findLastBalanceByAccount(Account account);
}
