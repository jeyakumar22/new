package com.example.GeneralLedger.DTO.Journal;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class JournalEntryDTO {
    private LocalDateTime entryDate;
    private String description;
    private String referenceNumber;
    private List<JournalLineDTO> journalLineDTOS;
}

