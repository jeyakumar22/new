package com.example.GeneralLedger.DTO.Journal;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JournalLineResponseDTO {
    private Long id;
    private String accountCode;
    private String accountName;
    private BigDecimal debit;
    private BigDecimal credit;
    private String description;
    private Long transactionId;
    private String journalEntryNumber;
    private LocalDateTime entryDate;
}