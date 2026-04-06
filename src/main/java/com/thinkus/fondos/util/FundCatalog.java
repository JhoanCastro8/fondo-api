package com.thinkus.fondos.util;

import com.thinkus.fondos.domain.Fund;

import java.math.BigDecimal;
import java.util.Map;

public class FundCatalog {

    public static final Map<String, Fund> FUNDS = Map.of(
            "1", new Fund("1", "FPV_BTG_PACTUAL_RECAUDADORA", new BigDecimal("75000"), "FPV"),
            "2", new Fund("2", "FPV_BTG_PACTUAL_ECOPETROL", new BigDecimal("125000"), "FPV"),
            "3", new Fund("3", "DEUDAPRIVADA", new BigDecimal("50000"), "FIC"),
            "4", new Fund("4", "FDO-ACCIONES", new BigDecimal("250000"), "FIC"),
            "5", new Fund("5", "FPV_BTG_PACTUAL_DINAMICA", new BigDecimal("100000"), "FPV")
    );

}
