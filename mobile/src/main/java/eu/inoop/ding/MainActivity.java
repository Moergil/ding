package eu.inoop.ding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.disposables.ListCompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_REQUEST_RECORD_AUDIO = 0;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Nullable
    private DingCore mDingCore;
    @Nullable
    private ListCompositeDisposable mDisposables;

    @BindView(R.id.txt_value)
    TextView mTextView;
    @BindView(R.id.btn_start)
    Button mStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDingCore = DingCore.getInstance(getApplicationContext());
        mDisposables = new ListCompositeDisposable();

        ButterKnife.bind(this);

        final Disposable disposable = WebService.waitForPayment("abc")
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        mTextView.setText("PAID");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mTextView.setText("ERROR");
                    }
                });
        mDisposables.add(disposable);
    }

    private boolean doWeHaveRecordAudioPermission() {
        /*------------------------------------------------------------------------------
         * Audio permissions are required for Chirp I/O.
         *----------------------------------------------------------------------------*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RESULT_REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*------------------------------------------------------------------------------
         * When foregrounded, start audio processing with chirpSDK.start().
         * This is required to send or receive chirps.
         *----------------------------------------------------------------------------*/
        if (doWeHaveRecordAudioPermission()) {
            mDingCore.resume();

            Disposable disposable = mDingCore.startListening()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<DingMessage>() {
                        @Override
                        public void accept(DingMessage dingMessage) throws Exception {
                            mTextView.setText(dingMessage.toString());
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("E", throwable.toString());
                        }
                    });
            mDisposables.add(disposable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mDisposables != null && !mDisposables.isDisposed()) {
            mDisposables.dispose();
        }
        mDingCore.pause();
    }

    @OnClick(R.id.btn_pay)
    void onPayClick() {
        WebService.pay("abc");
    }

    @OnClick(R.id.btn_start)
    void onStartClick() {
        final Random random = new Random();
        final String key = "" + random.nextInt();

        final float price = random.nextFloat() * 100;
        final String desc = "Pay me!";

        final DingMessage dingMessage = new DingMessage(key, price, desc);

        mDingCore.send(dingMessage);
        mTextView.setText("Sent: " + dingMessage);
    }

    @OnClick(R.id.btn_reset)
    void onResetClick() {
        mTextView.setText("");
    }
}
