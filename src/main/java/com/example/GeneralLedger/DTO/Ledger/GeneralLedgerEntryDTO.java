package com.example.GeneralLedger.DTO.Ledger;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeneralLedgerEntryDTO {
    private String accountName;
    private String accountCode;
    private String accountType;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balance;
}
