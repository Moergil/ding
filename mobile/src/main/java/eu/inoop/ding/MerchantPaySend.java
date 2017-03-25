package eu.inoop.ding;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inoop.ding.logic.DingCore;
import eu.inoop.ding.logic.DingMessage;
import eu.inoop.ding.logic.WebService;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MerchantPaySend extends BaseActivity {

    public static final String STATE_KEY_AMOUNT = "StateKeyAmount";
    public static final String STATE_KEY_PAYMENT_SEND = "StateKeyPaymentSend";

    private StringBuilder mAmountBuilder = new StringBuilder();

    @BindView(R.id.payment_request_preparation_pane)
    View mPaymentRequestPreparationPane;

    @BindView(R.id.payment_send_status_pane)
    View mPaymentSendStatusPane;

    @BindView(R.id.send_progress)
    ImageView mSendProgress;

    @BindView(R.id.payment_send_status_text)
    TextView mSendStatusText;

    @BindView((R.id.amount_text))
    TextView mAmountText;

    private Handler mHandler = new Handler();

    private Animation mRotateAnimation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        mRotateAnimation.setFillAfter(true);

        setContentView(R.layout.activity_merchant_pay_send);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            String savedAmout = savedInstanceState.getString(STATE_KEY_AMOUNT);
            mAmountBuilder.append(savedAmout);

            boolean paymentSend = savedInstanceState.getBoolean(STATE_KEY_PAYMENT_SEND);
            if (paymentSend) {
                mPaymentSendStatusPane.setVisibility(View.VISIBLE);
            } else {
                mPaymentRequestPreparationPane.setVisibility(View.VISIBLE);
            }
        } else {
            mPaymentRequestPreparationPane.setVisibility(View.VISIBLE);
        }

        refreshAmount();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_KEY_AMOUNT, mAmountBuilder.toString());
    }

    private void refreshAmount() {
        int length = mAmountBuilder.length();
        if (length == 0) {
            mAmountText.setText("0");
        } else {
            String sumString = mAmountBuilder.toString();
            mAmountText.setText(sumString);
        }
    }

    private void showProgress() {
        mSendProgress.setImageResource(R.drawable.ic_loader);
        mSendProgress.startAnimation(mRotateAnimation);
        mSendStatusText.setText("Waiting...");
    }

    private void showFinished() {
        mSendProgress.setImageResource(R.drawable.ic_wincheck);
        mSendProgress.clearAnimation();
        mSendStatusText.setText("Paid!");
    }

    @OnClick(R.id.send_button)
    public void onSendClick(View view) {
        mPaymentRequestPreparationPane.setVisibility(View.GONE);
        mPaymentSendStatusPane.setVisibility(View.VISIBLE);

        showProgress();

        final String key = UUID.randomUUID().toString();
        final float price = Float.parseFloat(mAmountText.getText().toString());
        final String desc = "some info";
        final DingMessage dingMessage = new DingMessage(key, price, desc);

        DingCore.getInstance(getApplicationContext())
                .send(dingMessage);

        final Disposable disposable = WebService.waitForPayment(key)
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        showFinished();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("a", throwable.getMessage());
                    }
                });
        mDisposables.add(disposable);
    }

    @OnClick({
            R.id.button_keypad_1,
            R.id.button_keypad_2,
            R.id.button_keypad_3,
            R.id.button_keypad_4,
            R.id.button_keypad_5,
            R.id.button_keypad_6,
            R.id.button_keypad_7,
            R.id.button_keypad_8,
            R.id.button_keypad_9,
            R.id.button_keypad_dot,
            R.id.button_keypad_0,
            R.id.button_keypad_delete})
    public void onKeypadClick(View view) {
        int id = view.getId();

        if (id == R.id.button_keypad_delete) {
            int length = mAmountBuilder.length();
            if ((length > 0)) {
                mAmountBuilder.deleteCharAt(length - 1);
            }
            refreshAmount();
            return;
        }

        if (id == R.id.button_keypad_dot) {
            return; // TODO
        }

        char c;
        switch (id) {
            case R.id.button_keypad_0:
                c = '0';
                break;
            case R.id.button_keypad_1:
                c = '1';
                break;
            case R.id.button_keypad_2:
                c = '2';
                break;
            case R.id.button_keypad_3:
                c = '3';
                break;
            case R.id.button_keypad_4:
                c = '4';
                break;
            case R.id.button_keypad_5:
                c = '5';
                break;
            case R.id.button_keypad_6:
                c = '6';
                break;
            case R.id.button_keypad_7:
                c = '7';
                break;
            case R.id.button_keypad_8:
                c = '8';
                break;
            case R.id.button_keypad_9:
                c = '9';
                break;
            default:
                throw new IllegalArgumentException("Not supported button clicked.");
        }

        mAmountBuilder.append(c);

        refreshAmount();
    }

}
