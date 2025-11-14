package com.example.GeneralLedger.Repository;

import com.example.GeneralLedger.Enum.JournalStatus;
import com.example.GeneralLedger.Model.Account;
import com.example.GeneralLedger.Model.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JournalLineRepository extends JpaRepository<JournalLine , Long> {
    @Query("SELECT COALESCE(MAX(jl.transactionId), 0) + 1 FROM JournalLine jl")
    Long findMaxTransactionId();

    List<JournalLine> findByAccountAndJournalEntryStatus(Account account, JournalStatus journalStatus);
}
