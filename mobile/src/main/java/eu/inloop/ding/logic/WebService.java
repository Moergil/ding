package eu.inloop.ding.logic;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;


public final class WebService {

    private static final String TAG = WebService.class.getSimpleName();

    public static void pay(@NonNull final String key) {
        // Write a message to the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference(key);
        myRef.setValue(true);
    }

    @NonNull
    public static Completable waitForPayment(@NonNull final String key) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final @NonNull CompletableEmitter emitter) throws Exception {
                // Write a message to the database
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference(key);

                // Read from the database
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        try {
                            final Boolean paid = dataSnapshot.getValue(Boolean.class);
                            Log.d(TAG, "Value is: " + paid);
                            if (Boolean.TRUE.equals(paid)) {
                                emitter.onComplete();
                            }
                        } catch (Exception e) {
                            // ignore invalid value
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                        emitter.onError(error.toException());
                    }
                });
            }
        });
    }
}
