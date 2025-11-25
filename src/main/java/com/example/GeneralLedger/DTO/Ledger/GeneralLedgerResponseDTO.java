package com.example.GeneralLedger.DTO.Ledger;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GeneralLedgerResponseDTO {
    private List<GeneralLedgerEntryDTO> accounts;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balanceDifference;
}