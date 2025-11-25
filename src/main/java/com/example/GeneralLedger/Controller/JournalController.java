package com.example.GeneralLedger.Controller;

import com.example.GeneralLedger.DTO.Journal.JournalEntryDTO;
import com.example.GeneralLedger.DTO.Journal.JournalEntryResponseDTO;
import com.example.GeneralLedger.Service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping("/entries")
    public ResponseEntity<JournalEntryResponseDTO> createJournalEntry(
            @RequestBody JournalEntryDTO request) {
        JournalEntryResponseDTO entry = journalService.createJournalEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @GetMapping("/entries")
    public ResponseEntity<List<JournalEntryResponseDTO>> getAllJournalEntries() {
        List<JournalEntryResponseDTO> entries = journalService.getAllJournalEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/entries/{entryId}")
    public ResponseEntity<JournalEntryResponseDTO> getJournalEntryById(@PathVariable Long entryId) {
        JournalEntryResponseDTO entry = journalService.getJournalEntryById(entryId);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("/entries/post/{entryId}")
    public ResponseEntity<JournalEntryResponseDTO> postJournalEntry(@PathVariable Long entryId) {
        JournalEntryResponseDTO entry = journalService.postJournalEntry(entryId);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/entries/status/{status}")
    public ResponseEntity<List<JournalEntryResponseDTO>> getJournalEntriesByStatus(
            @PathVariable String status) {
        List<JournalEntryResponseDTO> entries = journalService.getJournalEntriesByStatus(status);
        return ResponseEntity.ok(entries);
    }
}