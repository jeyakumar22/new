package com.example.GeneralLedger.Model;

import com.example.GeneralLedger.Enum.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_code", unique = true, nullable = false)
    private String accountCode;

    @Column(nullable = false)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
