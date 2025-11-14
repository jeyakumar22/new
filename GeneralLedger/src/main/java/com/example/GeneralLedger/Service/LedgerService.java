package com.example.GeneralLedger.Service;


import com.example.GeneralLedger.DTO.Ledger.GeneralLedgerEntryDTO;
import com.example.GeneralLedger.DTO.Ledger.GeneralLedgerResponseDTO;
import com.example.GeneralLedger.DTO.Ledger.LedgerEntryDTO;
import com.example.GeneralLedger.DTO.Ledger.LedgerResponseDTO;
import com.example.GeneralLedger.Enum.AccountType;
import com.example.GeneralLedger.Enum.JournalStatus;
import com.example.GeneralLedger.Model.Account;
import com.example.GeneralLedger.Model.JournalLine;
import com.example.GeneralLedger.Repository.AccountRepository;
import com.example.GeneralLedger.Repository.JournalLineRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LedgerService {
    private final AccountRepository accountRepository;
    private final JournalLineRepository journalLineRepository;

    public LedgerResponseDTO getAccountLedger(String accountCode) {

        Optional<Account> accountOpt = accountRepository.findByAccountCode(accountCode);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Account not found: " + accountCode);
        }

        Account account = accountOpt.get();

        List<JournalLine> transactions = journalLineRepository.findByAccountAndJournalEntryStatus(
                account, JournalStatus.POSTED);

        List<LedgerEntryDTO> ledgerEntries = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (JournalLine transaction : transactions) {

            BigDecimal change = calculateBalance(account.getType(), transaction.getDebit(), transaction.getCredit());

            runningBalance = runningBalance.add(change);

            totalDebit = totalDebit.add(transaction.getDebit());
            totalCredit = totalCredit.add(transaction.getCredit());

            LedgerEntryDTO entry = new LedgerEntryDTO();
            entry.setDate(transaction.getJournalEntry().getEntryDate());
            entry.setJournalEntryNumber(transaction.getJournalEntry().getEntryNumber());
            entry.setDescription(transaction.getDescription());
            entry.setDebit(transaction.getDebit());
            entry.setCredit(transaction.getCredit());
            entry.setRunningBalance(runningBalance);
            ledgerEntries.add(entry);
        }

        LedgerResponseDTO response = new LedgerResponseDTO();
        response.setAccountName(account.getAccountName());
        response.setAccountCode(account.getAccountCode());
        response.setAccountType(account.getType().name());
        response.setEntries(ledgerEntries);
        response.setTotalDebit(totalDebit);
        response.setTotalCredit(totalCredit);
        response.setClosingBalance(runningBalance);

        return response;
    }

    public GeneralLedgerResponseDTO getGeneralLedger() {
        List<Account> allAccounts = accountRepository.findAllByOrderByAccountCodeAsc();
        List<GeneralLedgerEntryDTO> generalLedgerEntries = new ArrayList<>();

        BigDecimal grandTotalDebit = BigDecimal.ZERO;
        BigDecimal grandTotalCredit = BigDecimal.ZERO;

        for (Account account : allAccounts) {

            List<JournalLine> transactions = journalLineRepository.findByAccountAndJournalEntryStatus(
                    account, JournalStatus.POSTED);

            BigDecimal totalDebit = transactions.stream()
                    .map(JournalLine::getDebit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredit = transactions.stream()
                    .map(JournalLine::getCredit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            BigDecimal balance = calculateBalance(account.getType(), totalDebit, totalCredit);

            grandTotalDebit = grandTotalDebit.add(totalDebit);
            grandTotalCredit = grandTotalCredit.add(totalCredit);

            GeneralLedgerEntryDTO entry = new GeneralLedgerEntryDTO();
            entry.setAccountName(account.getAccountName());
            entry.setAccountCode(account.getAccountCode());
            entry.setAccountType(account.getType().name());
            entry.setTotalDebit(totalDebit);
            entry.setTotalCredit(totalCredit);
            entry.setBalance(balance);
            generalLedgerEntries.add(entry);
        }

        GeneralLedgerResponseDTO response = new GeneralLedgerResponseDTO();
        response.setAccounts(generalLedgerEntries);
        response.setTotalDebit(grandTotalDebit);
        response.setTotalCredit(grandTotalCredit);
        response.setBalanceDifference(grandTotalDebit.subtract(grandTotalCredit));

        return response;
    }

    private BigDecimal calculateBalance(AccountType type, BigDecimal debit, BigDecimal credit) {
        switch (type) {
            case ASSET:
            case EXPENSE:
                return debit.subtract(credit); // Normal debit balance
            case LIABILITY:
            case EQUITY:
            case INCOME:
                return credit.subtract(debit); // Normal credit balance
            default:
                return BigDecimal.ZERO;
        }
    }
}