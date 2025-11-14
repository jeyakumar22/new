package com.example.GeneralLedger.Repository;

import com.example.GeneralLedger.Enum.AccountType;
import com.example.GeneralLedger.Model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    //boolean existsByAccountCode(String accountCode);

    //Optional<Account> findByAccountName(String accountName);

    Optional<Account> findTopByTypeOrderByAccountCodeDesc(AccountType type);

    List<Account> findAllByOrderByAccountCodeAsc();

    Optional<Account> findByAccountCode(String accountCode);

    List<Account> findByTypeOrderByAccountCodeAsc(AccountType type);

    boolean existsByAccountName(String accountName);

    boolean existsByAccountCode(String accountCode);
}
