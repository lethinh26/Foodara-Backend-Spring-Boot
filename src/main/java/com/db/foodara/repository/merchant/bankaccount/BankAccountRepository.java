package com.db.foodara.repository.merchant.bankaccount;

import com.db.foodara.entity.merchant.bankaccount.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    Optional<List<BankAccount>> getBankAccountsByAccountHolder(String merchantId);

    Optional<BankAccount> getBankAccountsById(String id);
    boolean existsByAccountNumber(String accountNumber);
}
