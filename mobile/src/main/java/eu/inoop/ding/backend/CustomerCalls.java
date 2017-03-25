package eu.inoop.ding.backend;

import java.math.BigDecimal;

public interface CustomerCalls {
    void pay(String currencyCode, BigDecimal sum, String merchantId);
}
