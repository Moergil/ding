package eu.inoop.ding;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Currency;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inoop.ding.logic.DingCore;
import eu.inoop.ding.logic.DingMessage;
import eu.inoop.ding.logic.WebService;
import eu.inoop.ding.model.PaymentInfo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public final class CustomerPayReceiveActivity extends BaseActivity {

    public static final String STATE_KEY_PAYMENT_INFO = "StateKeyPaymentInfo";

    @BindView(R.id.payment_await_pane)
    RelativeLayout mPaymentAwaitPane;

    @BindView((R.id.payment_pane))
    RelativeLayout mPaymentPane;

    @BindView(R.id.merchant_logo)
    ImageView mMerchantLogo;

    @BindView((R.id.payment_sum))
    TextView mPaymentSum;

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
    }

    @Override
    protected void onDingCoreResumed() {
        super.onDingCoreResumed();

        Disposable disposable = DingCore.getInstance(getApplicationContext()).startListening()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<DingMessage>() {
                    @Override
                    public void accept(DingMessage dingMessage) throws Exception {
                        PaymentInfo paymentInfo = new PaymentInfo(
                                dingMessage.getKey(),
                                Currency.getInstance("USD"),
                                new BigDecimal(dingMessage.getPrice())
                        );
                        paymentReceived(paymentInfo);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("E", throwable.toString());
                    }
                });
        mDisposables.add(disposable);
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

    @OnClick(R.id.pay_action_confirm)
    void onConfirmPayment() {
        if (mPaymentInfo != null) {
            WebService.pay(mPaymentInfo.getMerchantId());
            this.onBackPressed();
        }
    }

    @OnClick(R.id.pay_action_cancel)
    void onCancelPayment() {
        this.onBackPressed();
    }
}
