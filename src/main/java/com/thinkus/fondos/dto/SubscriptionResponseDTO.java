package com.thinkus.fondos.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionResponseDTO {

    private String message;
    private String status;

}
