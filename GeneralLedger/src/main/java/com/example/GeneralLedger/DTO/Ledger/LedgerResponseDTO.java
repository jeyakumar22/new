package com.example.GeneralLedger.DTO.Ledger;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LedgerResponseDTO {
    private String accountName;
    private String accountCode;
    private String accountType;
    private List<LedgerEntryDTO> entries;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal closingBalance;
}

