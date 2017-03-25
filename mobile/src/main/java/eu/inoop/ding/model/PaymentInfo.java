package eu.inoop.ding.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

public class PaymentInfo implements Serializable {

    private final String mMerchantId;
    private final Currency mCurrrency;
    private final BigDecimal mPaymentSum;

    public PaymentInfo(String mMerchantId, Currency mCurrrency, BigDecimal mPaymentSum) {
        this.mMerchantId = mMerchantId;
        this.mCurrrency = mCurrrency;
        this.mPaymentSum = mPaymentSum;
    }

    public String getMerchantId() {
        return mMerchantId;
    }

    public Currency getCurrrency() {
        return mCurrrency;
    }

    public BigDecimal getPaymentSum() {
        return mPaymentSum;
    }
}
