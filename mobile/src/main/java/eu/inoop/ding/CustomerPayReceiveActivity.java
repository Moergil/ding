package eu.inoop.ding;

import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inoop.ding.model.PaymentInfo;

public class CustomerPayReceiveActivity extends AppCompatActivity {

    public static final String STATE_KEY_PAYMENT_INFO = "StateKeyPaymentInfo";

    @BindView(R.id.payment_await_pane)
    RelativeLayout mPaymentAwaitPane;

    @BindView((R.id.payment_pane))
    RelativeLayout mPaymentPane;

    @BindView(R.id.merchant_logo)
    ImageView mMerchantLogo;

    @BindView((R.id.payment_sum))
    TextView mPaymentSum;

    private Handler mHandler = new Handler();

    @Nullable
    private PaymentInfo mPaymentInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPaymentInfo = (PaymentInfo) savedInstanceState.getSerializable(STATE_KEY_PAYMENT_INFO);
        }

        setContentView(R.layout.activity_customer_pay_receive);

        ButterKnife.bind(this);

        if (mPaymentInfo != null) {
            showPaymentInfoPane();
        } else {
            mPaymentAwaitPane.setVisibility(View.VISIBLE);
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PaymentInfo paymentInfo = new PaymentInfo(
                        UUID.randomUUID().toString(),
                        Currency.getInstance("USD"),
                        new BigDecimal(200)
                );
                paymentReceived(paymentInfo);
            }
        }, 2000);
    }

    private void paymentReceived(PaymentInfo paymentInfo) {
        mPaymentInfo = paymentInfo;
        showPaymentInfoPane();
    }

    private void showPaymentInfoPane() {
        mPaymentAwaitPane.setVisibility(View.GONE);
        mPaymentPane.setVisibility(View.VISIBLE);

        String paymentSumString = mPaymentInfo.getPaymentSum() + " " + mPaymentInfo.getCurrrency().getCurrencyCode();
        mPaymentSum.setText(paymentSumString);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putSerializable(STATE_KEY_PAYMENT_INFO, mPaymentInfo);
    }
}
