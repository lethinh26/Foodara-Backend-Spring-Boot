package com.db.foodara.entity.merchant.bankaccount;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "store_bank_account")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;

    @Column(nullable = false)
    private String branch;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = true;
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = true;


}
