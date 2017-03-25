package eu.inoop.ding.logic;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import eu.inoop.ding.R;
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
    private final Subject<DingMessage> mReadSubject = PublishSubject.create();
    @NonNull
    private final Gson mGson = new Gson();

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

    public Observable<DingMessage> startListening() {
        return mReadSubject;
    }

    public void send(@NonNull final DingMessage dingMessage) {
        final String jsonMessage = mGson.toJson(dingMessage);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("value", jsonMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final Chirp chirpJson = new Chirp(jsonObject);
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

    private void readChirp(final Chirp chirp) {
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
                try {
                    final String jsonMessage = (String) chirp.getJsonData().get("value");
                    Log.d(TAG, "onReadResponse: " + jsonMessage);
                    final DingMessage dingMessage = mGson.fromJson(jsonMessage, DingMessage.class);
                    mReadSubject.onNext(dingMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
