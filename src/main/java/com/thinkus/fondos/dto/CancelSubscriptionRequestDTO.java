package com.thinkus.fondos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelSubscriptionRequestDTO {

    @NotBlank
    private String clientId;

    @NotBlank
    private String fundId;

}
