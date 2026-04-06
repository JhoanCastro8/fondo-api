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
public class Subscription {

    private String clientId;
    private String fundId;
    private BigDecimal amount;
    private String status;

}
