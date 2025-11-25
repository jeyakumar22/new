package com.example.GeneralLedger.DTO.Ledger;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LedgerEntryDTO {
    private LocalDateTime date;
    private String journalEntryNumber;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal runningBalance;
}

