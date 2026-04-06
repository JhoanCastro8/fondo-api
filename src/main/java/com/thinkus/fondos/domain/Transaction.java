package com.thinkus.fondos.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    private String transactionId;
    private String clientId;
    private String fundId;
    private String type;
    private BigDecimal amount;
    private String timestamp;

}
