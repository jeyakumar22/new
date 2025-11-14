package com.example.GeneralLedger.Service;

import com.example.GeneralLedger.DTO.Journal.JournalEntryDTO;
import com.example.GeneralLedger.DTO.Journal.JournalEntryResponseDTO;
import com.example.GeneralLedger.DTO.Journal.JournalLineDTO;
import com.example.GeneralLedger.Enum.JournalStatus;
import com.example.GeneralLedger.Model.Account;
import com.example.GeneralLedger.Model.GeneralLedgerEntry;
import com.example.GeneralLedger.Model.JournalEntry;
import com.example.GeneralLedger.Model.JournalLine;
import com.example.GeneralLedger.Repository.AccountRepository;
import com.example.GeneralLedger.Repository.GLEntryRepository;
import com.example.GeneralLedger.Repository.JournalLineRepository;
import com.example.GeneralLedger.Repository.JournalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;
    private final JournalLineRepository journalLineRepository;
    private final GLEntryRepository glEntryRepository;

    public JournalEntryResponseDTO createJournalEntry(JournalEntryDTO request) {
        List<JournalLineDTO> lines = request.getJournalLineDTOS();

        if (lines == null || lines.size() < 2) {
            throw new RuntimeException("Journal entry must have at least TWO lines (debit and credit).");
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (JournalLineDTO line : lines) {
            totalDebit = totalDebit.add(line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO);
            totalCredit = totalCredit.add(line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new RuntimeException("Total debit and credit must be equal");
        }

        for (JournalLineDTO lineDTO : lines) {
            if (!accountRepository.existsByAccountCode(lineDTO.getAccountCode())) {
                throw new RuntimeException("Account not found: " + lineDTO.getAccountCode());
            }
        }

        JournalEntry entry = new JournalEntry();
        entry.setEntryNumber(generateEntryNumber());
        entry.setEntryDate(request.getEntryDate());
        entry.setDescription(request.getDescription());
        entry.setReferenceNumber(request.getReferenceNumber());
        entry.setStatus(JournalStatus.DRAFT);

        Long nextTransactionId = getNextTransactionId();

        for (JournalLineDTO lineDTO : lines) {
            Account account = accountRepository.findByAccountCode(lineDTO.getAccountCode())
                    .orElseThrow(() -> new RuntimeException("Account not found with code: " + lineDTO.getAccountCode()));

            JournalLine line = new JournalLine();
            line.setJournalEntry(entry);
            line.setAccount(account);
            line.setDebit(lineDTO.getDebit() != null ? lineDTO.getDebit() : BigDecimal.ZERO);
            line.setCredit(lineDTO.getCredit() != null ? lineDTO.getCredit() : BigDecimal.ZERO);
            line.setTransactionId(nextTransactionId);
            line.setDescription(lineDTO.getDescription());

            entry.getJournalLines().add(line);
        }

        JournalEntry savedEntry = journalRepository.save(entry);

        return convertToResponse(savedEntry);
    }


    private Long getNextTransactionId() {
        return journalLineRepository.findMaxTransactionId();
    }

    private String generateEntryNumber() {

        String lastEntryNumber = journalRepository.findLastEntryNumber();


        if (lastEntryNumber == null) {
            return "JE-" + LocalDate.now().getYear() + "-0001";
        }


        String[] parts = lastEntryNumber.split("-");
        int lastNumber = Integer.parseInt(parts[2]);

        int nextNumber = lastNumber + 1;

        return "JE-" + LocalDate.now().getYear() + "-" + String.format("%04d", nextNumber);
    }


    public List<JournalEntryResponseDTO> getAllJournalEntries() {
        List<JournalEntry> entries = journalRepository.findAllByOrderByEntryDateDesc();
        return convertToResponseList(entries);
    }

    public JournalEntryResponseDTO getJournalEntryById(Long entryId) {
        Optional<JournalEntry> entryOptional = journalRepository.findById(entryId);

        if (entryOptional.isEmpty()) {
            throw new RuntimeException("Journal entry not found with id: " + entryId);
        }

        return convertToResponse(entryOptional.get());
    }

    public JournalEntryResponseDTO postJournalEntry(Long entryId) {

        JournalEntry entry = journalRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Journal entry not found: " + entryId));

        if (entry.getStatus() != JournalStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT entries can be posted");
        }

        // 1) Update entry status
        entry.setStatus(JournalStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());

        // Save journal entry
        JournalEntry savedEntry = journalRepository.save(entry);

        // 2) Create GL Entries
        for (JournalLine line : savedEntry.getJournalLines()) {
            createGLEntry(line, savedEntry);
        }

        return convertToResponse(savedEntry);
    }

    private void createGLEntry(JournalLine line, JournalEntry journalEntry) {

        Account account = line.getAccount();

        // Fetch previous running balance (NULL safe)
        BigDecimal previousBalance =
                Optional.ofNullable(glEntryRepository.findLastBalanceByAccount(account))
                        .orElse(BigDecimal.ZERO);

        // Calculate change
        BigDecimal debit = line.getDebit() == null ? BigDecimal.ZERO : line.getDebit();
        BigDecimal credit = line.getCredit() == null ? BigDecimal.ZERO : line.getCredit();

        BigDecimal newBalance;

        switch (account.getType()) {
            case ASSET:
            case EXPENSE:
                newBalance = previousBalance.add(debit).subtract(credit);
                break;

            case LIABILITY:
            case EQUITY:
            case INCOME:
                newBalance = previousBalance.add(credit).subtract(debit);
                break;

            default:
                newBalance = previousBalance;
        }

        // Build GL entry
        GeneralLedgerEntry gl = new GeneralLedgerEntry();
        gl.setAccount(account);
        gl.setJournalEntry(journalEntry);
        gl.setEntryDate(LocalDate.from(journalEntry.getEntryDate()));
        gl.setDescription(line.getDescription());
        gl.setDebit(debit);
        gl.setCredit(credit);
        gl.setRunningBalance(newBalance);

        // Save
        glEntryRepository.save(gl);
    }



    public List<JournalEntryResponseDTO> getJournalEntriesByStatus(String status) {
        JournalStatus status1 = JournalStatus.valueOf(status.toUpperCase());
        List<JournalEntry> entries = journalRepository.findByStatusOrderByEntryDateDesc(status1);
        return convertToResponseList(entries);
    }


    private JournalEntryResponseDTO convertToResponse(JournalEntry entry) {
        JournalEntryResponseDTO response = new JournalEntryResponseDTO();
        response.setId(entry.getId());
        response.setEntryNumber(entry.getEntryNumber());
        response.setEntryDate(entry.getEntryDate());
        response.setDescription(entry.getDescription());
        response.setReferenceNumber(entry.getReferenceNumber());
        response.setStatus(entry.getStatus());
        response.setPostedAt(entry.getPostedAt());
        return response;
    }

    private List<JournalEntryResponseDTO> convertToResponseList(List<JournalEntry> entries) {
        return entries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}