package com.thinkus.fondos.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Client {

    private String clientId;
    private String name;
    private BigDecimal balance;
    private String notificationPreference;

}
