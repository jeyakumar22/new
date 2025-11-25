package com.example.GeneralLedger.Service;

import com.example.GeneralLedger.Enum.AccountType;
import com.example.GeneralLedger.Model.Account;
import com.example.GeneralLedger.Repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(Account request) {

        if (accountRepository.existsByAccountName(request.getAccountName())) {
            throw new RuntimeException("Account name already exists: " + request.getAccountName());
        }

        String generatedCode = generateAccountCode(request.getType());

        Account account = new Account();
        account.setAccountCode(generatedCode);
        account.setAccountName(request.getAccountName());
        account.setType(request.getType());
        account.setDescription(request.getDescription());


        return (accountRepository.save(account));
    }
    private String generateAccountCode(AccountType type) {
        int prefix;

        switch (type) {
            case ASSET:
                prefix = 1000;
                break;
            case LIABILITY:
                prefix = 2000;
                break;
            case EQUITY:
                prefix = 3000;
                break;
            case INCOME:
                prefix = 4000;
                break;
            case EXPENSE:
                prefix = 5000;
                break;
            default:
                throw new RuntimeException("Unknown account type: " + type);
        }


        Optional<Account> lastAccount = accountRepository.findTopByTypeOrderByAccountCodeDesc(type);

        int newCode;

        if (lastAccount.isEmpty()) {

            newCode = prefix + 1;
        } else {
            String lastCodeStr = lastAccount.get().getAccountCode();
            newCode = Integer.parseInt(lastCodeStr) + 1;
        }

        return String.valueOf(newCode);
    }




    public List<Account> getAllAccounts() {
        return accountRepository.findAllByOrderByAccountCodeAsc();
    }

    public Account getAccountByCode(String accountCode) {
        return accountRepository.findByAccountCode(accountCode)
                .orElseThrow(() -> new RuntimeException("Account not found with code: " + accountCode));
    }

    public List<Account> getAccountsByType(String type) {
        try {
            AccountType type1 = AccountType.valueOf(type.toUpperCase());
            return accountRepository.findByTypeOrderByAccountCodeAsc(type1);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid account type: " + type);
        }
    }

}