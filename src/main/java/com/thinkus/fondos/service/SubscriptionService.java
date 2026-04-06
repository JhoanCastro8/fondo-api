package com.thinkus.fondos.service;

import com.thinkus.fondos.dto.SubscribeRequestDTO;
import com.thinkus.fondos.dto.SubscriptionResponseDTO;
import com.thinkus.fondos.dto.TransactionResponseDTO;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponseDTO subscribe(SubscribeRequestDTO request);
    void cancel(String clientId, String fundId);
    List<TransactionResponseDTO> getTransactions(String clientId);

}
