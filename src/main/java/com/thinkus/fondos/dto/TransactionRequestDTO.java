package com.thinkus.fondos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransactionRequestDTO {

    @NotBlank
    private String clientId;

}
