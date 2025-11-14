package com.example.GeneralLedger.DTO.Journal;

import com.example.GeneralLedger.Enum.JournalStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JournalEntryResponseDTO {
    private Long id;
    private String entryNumber;
    private String description;
    private String referenceNumber;
    private JournalStatus status;
    private LocalDateTime entryDate;
    private LocalDateTime postedAt;
}
