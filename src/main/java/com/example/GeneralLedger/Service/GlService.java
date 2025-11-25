package com.example.GeneralLedger.Service;




import com.example.GeneralLedger.DTO.Ledger.GeneralLedgerEntryDTO;
import com.example.GeneralLedger.DTO.Ledger.GeneralLedgerResponseDTO;
import com.example.GeneralLedger.DTO.Ledger.LedgerEntryDTO;
import com.example.GeneralLedger.DTO.Ledger.LedgerResponseDTO;
import com.example.GeneralLedger.Model.Account;
import com.example.GeneralLedger.Model.GeneralLedgerEntry;
import com.example.GeneralLedger.Repository.AccountRepository;
import com.example.GeneralLedger.Repository.GLEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GlService {

    private final AccountRepository accountRepository;
    private final GLEntryRepository glEntryRepository;


    public LedgerResponseDTO getAccountLedger(String accountCode) {

        Account account = accountRepository.findByAccountCode(accountCode)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountCode));

        List<GeneralLedgerEntry> glEntries = glEntryRepository
                .findByAccountOrderByEntryDateAsc(account);

        List<LedgerEntryDTO> ledgerEntries = new ArrayList<>();

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (GeneralLedgerEntry gl : glEntries) {

            totalDebit = totalDebit.add(gl.getDebit() == null ? BigDecimal.ZERO : gl.getDebit());
            totalCredit = totalCredit.add(gl.getCredit() == null ? BigDecimal.ZERO : gl.getCredit());

            LedgerEntryDTO entry = new LedgerEntryDTO();

            entry.setDate(gl.getEntryDate());

            entry.setJournalEntryNumber(gl.getJournalEntry() != null ? gl.getJournalEntry().getEntryNumber() : null);
            entry.setDescription(gl.getDescription());
            entry.setDebit(gl.getDebit());
            entry.setCredit(gl.getCredit());
            entry.setRunningBalance(gl.getRunningBalance());

            ledgerEntries.add(entry);
        }

        LedgerResponseDTO response = new LedgerResponseDTO();
        response.setAccountName(account.getAccountName());
        response.setAccountCode(account.getAccountCode());
        response.setAccountType(account.getType().name());
        response.setEntries(ledgerEntries);
        response.setTotalDebit(totalDebit);
        response.setTotalCredit(totalCredit);


        response.setClosingBalance(
                glEntries.isEmpty() ? BigDecimal.ZERO :
                        glEntries.get(glEntries.size() - 1).getRunningBalance()
        );

        return response;
    }


    public GeneralLedgerResponseDTO getGeneralLedger() {

        List<Account> allAccounts = accountRepository.findAllByOrderByAccountCodeAsc();
        List<GeneralLedgerEntryDTO> summaries = new ArrayList<>();

        BigDecimal grandTotalDebit = BigDecimal.ZERO;
        BigDecimal grandTotalCredit = BigDecimal.ZERO;

        for (Account account : allAccounts) {

            BigDecimal totalDebit = glEntryRepository.sumDebitByAccount(account);
            BigDecimal totalCredit = glEntryRepository.sumCreditByAccount(account);

            BigDecimal lastBalance = glEntryRepository
                    .findLastBalanceByAccount(account);

            grandTotalDebit = grandTotalDebit.add(totalDebit);
            grandTotalCredit = grandTotalCredit.add(totalCredit);

            GeneralLedgerEntryDTO entry = new GeneralLedgerEntryDTO();
            entry.setAccountCode(account.getAccountCode());
            entry.setAccountName(account.getAccountName());
            entry.setAccountType(account.getType().name());
            entry.setTotalDebit(totalDebit);
            entry.setTotalCredit(totalCredit);
            entry.setBalance(lastBalance);

            summaries.add(entry);
        }

        GeneralLedgerResponseDTO summary = new GeneralLedgerResponseDTO();
        summary.setAccounts(summaries);
        summary.setTotalDebit(grandTotalDebit);
        summary.setTotalCredit(grandTotalCredit);
        summary.setBalanceDifference(grandTotalDebit.subtract(grandTotalCredit));

        return summary;
    }
}
