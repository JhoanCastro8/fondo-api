package com.thinkus.fondos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscribeRequestDTO {

    @NotBlank
    private String clientId;

    @NotBlank
    private String fundId;

    @NotNull
    private BigDecimal amount;

}
