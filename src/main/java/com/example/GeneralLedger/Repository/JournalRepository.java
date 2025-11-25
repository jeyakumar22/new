package com.example.GeneralLedger.Repository;


import com.example.GeneralLedger.Enum.JournalStatus;
import com.example.GeneralLedger.Model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JournalRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findAllByOrderByEntryDateDesc();

    List<JournalEntry> findByStatusOrderByEntryDateDesc(JournalStatus status);

    @Query("SELECT MAX(e.entryNumber) FROM JournalEntry e")
    String findLastEntryNumber();
}
