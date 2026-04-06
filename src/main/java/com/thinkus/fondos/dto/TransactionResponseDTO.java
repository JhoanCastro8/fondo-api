package com.thinkus.fondos.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionResponseDTO {

    private String fundId;
    private String type;
    private BigDecimal amount;
    private String timestamp;

}
