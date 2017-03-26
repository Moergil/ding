package eu.inloop.ding;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import eu.inloop.ding.logic.DingCore;
import io.reactivex.internal.disposables.ListCompositeDisposable;


public abstract class BaseActivity extends AppCompatActivity {

    private static final int RESULT_REQUEST_RECORD_AUDIO = 0;

    @Nullable
    protected ListCompositeDisposable mDisposables;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        DingCore.getInstance(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDisposables = new ListCompositeDisposable();

        /*------------------------------------------------------------------------------
         * When foregrounded, start audio processing with chirpSDK.start().
         * This is required to send or receive chirps.
         *----------------------------------------------------------------------------*/
        if (doWeHaveRecordAudioPermission()) {
            DingCore.getInstance(getApplicationContext()).resume();
            onDingCoreResumed();
        }
    }

    protected void onDingCoreResumed() {
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mDisposables != null && !mDisposables.isDisposed()) {
            mDisposables.dispose();
        }
        DingCore.getInstance(getApplicationContext()).pause();
    }

    private boolean doWeHaveRecordAudioPermission() {
        /*------------------------------------------------------------------------------
         * Audio permissions are required for Chirp I/O.
         *----------------------------------------------------------------------------*/
        final String permission = "android.permission.RECORD_AUDIO";
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, RESULT_REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }
}
