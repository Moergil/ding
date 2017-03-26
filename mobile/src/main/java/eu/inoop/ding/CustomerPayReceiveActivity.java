package eu.inoop.ding;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inoop.ding.logic.DingCore;
import eu.inoop.ding.logic.DingMessage;
import eu.inoop.ding.logic.WebService;
import eu.inoop.ding.model.PaymentInfo;
import io.chirp.sdk.AudioBufferListener;
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

    @BindView(R.id.payment_send_finished_pane)
    RelativeLayout mPaymentSendFinishedPane;

    @BindView(R.id.merchant_logo)
    ImageView mMerchantLogo;

    @BindView((R.id.payment_sum))
    TextView mPaymentSum;

    @Nullable
    private PaymentInfo mPaymentInfo;

    private ArrayList<View> mSoundLevelBlocks = new ArrayList<>();
    private ArrayList<Double> mSoundLevels = new ArrayList<>();
    private int mReferenceSize;
    private double mReferenceAmp = 1000;
    private double mMaxAmp = 20000;

    private int mSoundMargin;

    private AudioBufferListener mAudioBufferListener;

    private Handler mHandler = new Handler();

    private DingCore mDingCore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDingCore = DingCore.getInstance(this);

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

        float dp = 10;
        mReferenceSize = (int)(dp * getResources().getDisplayMetrics().density);

        float dp2 = 5;
        mSoundMargin = (int)(dp2 * getResources().getDisplayMetrics().density);

        mSoundLevelBlocks.add(ButterKnife.findById(this, R.id.audio_block_1));
        mSoundLevelBlocks.add(ButterKnife.findById(this, R.id.audio_block_2));
        mSoundLevelBlocks.add(ButterKnife.findById(this, R.id.audio_block_3));
        mSoundLevelBlocks.add(ButterKnife.findById(this, R.id.audio_block_4));
        mSoundLevelBlocks.add(ButterKnife.findById(this, R.id.audio_block_5));

        mSoundLevels.add(mReferenceAmp);
        mSoundLevels.add(mReferenceAmp);
        mSoundLevels.add(mReferenceAmp);
        mSoundLevels.add(mReferenceAmp);
        mSoundLevels.add(mReferenceAmp);

        mAudioBufferListener = new AudioBufferListener() {
            @Override
            public void onAudioBufferUpdate(int i, short[] shorts) {
                final double amplitude = calcAmplitude(shorts);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateSoundMeter(amplitude);
                    }
                });
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPaymentAwaitPane.getVisibility() == View.VISIBLE) {
            startMicrophoneListening();
        }
    }

    private double calcAmplitude(short[] buffer)
    {
        int max = 0;
        for (short s : buffer)
        {
            if (Math.abs(s) > max)
            {
                max = Math.abs(s);
            }
        }
        return max;
    }

    private void startMicrophoneListening() {
        mDingCore.addAudioListener(mAudioBufferListener);
    }

    private void updateSoundMeter(double newAmplitude) {
        if (newAmplitude > mMaxAmp) {
            newAmplitude = mMaxAmp;
        }

        int size = mSoundLevelBlocks.size();

        for (int i = 0; i < size - 1; i++) {
            double level = mSoundLevels.get(i + 1);
            mSoundLevels.set(i, level);
        }

        mSoundLevels.set(4, newAmplitude);

        for (int i = 0; i < size; i++) {
            View block = mSoundLevelBlocks.get(i);

            double amp = mSoundLevels.get(i);
            int chunks = (int)(amp / mReferenceAmp);
            int newHeight = chunks * mReferenceSize;
            int width = block.getWidth();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, newHeight);
            params.setMargins(mSoundMargin, mSoundMargin, mSoundMargin, mSoundMargin);
            block.setLayoutParams(params);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mDingCore.removeAudioListener(mAudioBufferListener);
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

        mDingCore.removeAudioListener(mAudioBufferListener);
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
            mPaymentPane.setVisibility(View.GONE);
            mPaymentSendFinishedPane.setVisibility(View.VISIBLE);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        }
    }

    @OnClick(R.id.pay_action_cancel)
    void onCancelPayment() {
        finish();
    }
}
