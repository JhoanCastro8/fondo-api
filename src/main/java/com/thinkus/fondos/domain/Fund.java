package com.thinkus.fondos.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Fund {

    private String id;
    private String name;
    private BigDecimal minAmount;
    private String category;

}
