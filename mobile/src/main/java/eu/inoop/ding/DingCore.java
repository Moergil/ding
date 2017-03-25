package eu.inoop.ding;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import io.chirp.sdk.CallbackCreate;
import io.chirp.sdk.CallbackRead;
import io.chirp.sdk.ChirpSDK;
import io.chirp.sdk.ChirpSDKListener;
import io.chirp.sdk.model.Chirp;
import io.chirp.sdk.model.ChirpError;
import io.chirp.sdk.model.ChirpProtocolName;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public final class DingCore {

    private static final String TAG = DingCore.class.getSimpleName();

    @Nullable
    private static DingCore sInstance = null;

    @NonNull
    public static DingCore getInstance(@NonNull final Context appContext) {
        if (null == sInstance) {
            sInstance = new DingCore(appContext);
        }
        return sInstance;
    }

    @NonNull
    private final Context mAppContext;
    @NonNull
    private final ChirpSDK mChirpSDK;
    @NonNull
    private final Subject<JSONObject> mReadSubject = PublishSubject.create();

    private final ChirpSDKListener mChirpSDKListener = new ChirpSDKListener() {

        /*------------------------------------------------------------------------------
         * onChirpHeard is triggered when a Chirp tone is received.
         * Obtain the chirp's 10-character identifier with `getIdentifier`.
         *----------------------------------------------------------------------------*/
        @Override
        public void onChirpHeard(Chirp chirp) {
            Log.d(TAG, "onChirpHeard: " + chirp.getIdentifier());

            /*------------------------------------------------------------------------------
             * As soon as we hear a chirp, query the API for its associated data.
             *----------------------------------------------------------------------------*/
            readChirp(chirp);
        }

        /*------------------------------------------------------------------------------
         * onChirpHearStarted is triggered when the beginning of Chirp tone is heard
         *----------------------------------------------------------------------------*/
        @Override
        public void onChirpHearStarted() {
            Log.d(TAG, "Chirp Hear Started");
        }

        /*------------------------------------------------------------------------------
        * onChirpHearFailed is triggered when the beginning of Chirp tone is heard but it
        * subsequently fails to decode the identifier
        *----------------------------------------------------------------------------*/
        @Override
        public void onChirpHearFailed() {
            Log.d(TAG, "Chirp Hear Failed");
        }

        /*------------------------------------------------------------------------------
         * onChirpError is triggered when an error occurs -- for example,
         * authentication failure or muted device.
         *
         * See the documentation on ChirpError for possible error codes.
         *----------------------------------------------------------------------------*/
        @Override
        public void onChirpError(ChirpError chirpError) {
            Log.d(TAG, "onChirpError: " + chirpError.getMessage());
        }
    };

    private DingCore(@NonNull final Context appContext) {
        mAppContext = appContext;

        mChirpSDK = new ChirpSDK(mAppContext,
                mAppContext.getString(R.string.chirp_app_key),
                mAppContext.getString(R.string.chirp_app_secret));
        mChirpSDK.setProtocolNamed(ChirpProtocolName.ChirpProtocolNameStandard);
        mChirpSDK.setListener(mChirpSDKListener);
    }

    public void resume() {
        mChirpSDK.start();
    }

    public void pause() {
        mChirpSDK.stop();
    }

    public Observable<JSONObject> startListening() {
        return mReadSubject;
    }

    public void send(final JSONObject json) {
        final Chirp chirpJson = new Chirp(json);
        mChirpSDK.create(chirpJson, new CallbackCreate() {

            @Override
            public void onCreateResponse(Chirp chirp) {
                mChirpSDK.chirp(chirp);
            }

            @Override
            public void onCreateError(ChirpError chirpError) {
                Log.e("chirt error", chirpError.getMessage());
            }
        });
    }

    private void readChirp(Chirp chirp) {
        /*------------------------------------------------------------------------------
         * ChirpSDK.read queries the Chirp API for extended data associated with a
         * given chirp. It requires an internet connection.
         *----------------------------------------------------------------------------*/
        mChirpSDK.read(chirp, new CallbackRead() {
            /*------------------------------------------------------------------------------
             * The associated data is a single JSON structured object of key-value pairs.
             * You can define arbitrary nested data structure within this.
             * Here, we simply retrieve the "text" key.
             *----------------------------------------------------------------------------*/
            @Override
            public void onReadResponse(final Chirp chirp) {
                final JSONObject jsonData = chirp.getJsonData();
                Log.d(TAG, "onReadResponse: " + jsonData);
                mReadSubject.onNext(jsonData);
            }

            /*------------------------------------------------------------------------------
             * If an error occurs contacting the Chirp API, generate an error.
             *----------------------------------------------------------------------------*/
            @Override
            public void onReadError(ChirpError chirpError) {
                Log.d(TAG, "onReadError: " + chirpError.getMessage());
            }
        });
    }
}
