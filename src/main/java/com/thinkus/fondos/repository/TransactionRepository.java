package com.thinkus.fondos.repository;

import com.thinkus.fondos.domain.Transaction;

import java.util.List;

public interface TransactionRepository {

    List<Transaction> findByClientId(String clientId);
    void save(Transaction transaction);

}
