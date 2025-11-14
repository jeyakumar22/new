package com.example.GeneralLedger.DTO.Journal;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalLineDTO {
    private String accountCode;  // "1010", "2100", etc.
    private BigDecimal debit;
    private BigDecimal credit;
    private String description;
}