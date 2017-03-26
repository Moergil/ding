package eu.inloop.ding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import blade.Blade;
import blade.Extra;
import blade.State;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.inloop.ding.logic.WebService;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

@Blade
public final class MerchantAwaitPaymentActivity extends BaseActivity {

    @Extra
    String mKey;

    @State
    boolean mPaid;

    private Animation mRotateAnimation;

    @BindView(R.id.send_progress)
    ImageView mSendProgress;

    @BindView(R.id.payment_send_status_text)
    TextView mSendStatusText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        mRotateAnimation.setFillAfter(true);

        setContentView(R.layout.activity_merchant_await_payment);

        ButterKnife.bind(this);

        if (mPaid) {
            showFinished();
        } else {
            showProgress();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mPaid) {
            final Disposable disposable = WebService.waitForPayment(mKey)
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
}
