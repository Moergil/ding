package eu.inoop.ding;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inoop.ding.model.PaymentInfo;
import eu.inoop.ding.util.SoundMeter;

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

    private AsyncTask<Void, Double, Void> mMicrophoneListening;

    private ArrayList<View> mSoundLevelBlocks = new ArrayList<>();
    private ArrayList<Double> mSoundLevels = new ArrayList<>();
    private int mReferenceSize;
    private double mReferenceAmp = 1000;
    private double mMaxAmp = 20000;

    private int mSoundMargin;

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
                // TODO paymentReceived(paymentInfo);
            }
        }, 2000);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPaymentAwaitPane.getVisibility() == View.VISIBLE) {
            mMicrophoneListening = new AsyncTask<Void, Double, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    SoundMeter soundMeter = new SoundMeter();

                    soundMeter.start();

                    while (!Thread.interrupted()) {
                        double amplitude = soundMeter.getAmplitude();
                        publishProgress(amplitude);
                    }

                    soundMeter.stop();

                    return null;
                }

                @Override
                protected void onProgressUpdate(Double... values) {
                    super.onProgressUpdate(values);

                    double actualAmp = values[0];
                    if (actualAmp > mMaxAmp) {
                        actualAmp = mMaxAmp;
                    }

                    int size = mSoundLevelBlocks.size();

                    for (int i = 0; i < size - 1; i++) {
                        double level = mSoundLevels.get(i + 1);
                        mSoundLevels.set(i, level);
                    }

                    mSoundLevels.set(4, actualAmp);

                    Log.v("AMP", "-----------------------" + actualAmp);
                    for (int i = 0; i < size; i++) {
                        View block = mSoundLevelBlocks.get(i);

                        double amp = mSoundLevels.get(i);
                        int chunks = (int)(amp / mReferenceAmp);
                        int newHeight = chunks * mReferenceSize;
                        int width = block.getWidth();

                        Log.v("AMP", "" + newHeight);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, newHeight);
                        params.setMargins(mSoundMargin, mSoundMargin, mSoundMargin, mSoundMargin);
                        block.setLayoutParams(params);
                    }
                }
            };
            mMicrophoneListening.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMicrophoneListening != null) {
            mMicrophoneListening.cancel(true);
            mMicrophoneListening = null;
        }
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

        if (mMicrophoneListening != null) {
            mMicrophoneListening.cancel(true);
            mMicrophoneListening = null;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putSerializable(STATE_KEY_PAYMENT_INFO, mPaymentInfo);
    }
}
