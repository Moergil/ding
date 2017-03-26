package eu.inloop.ding;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import java.util.UUID;

import blade.Blade;
import blade.I;
import blade.State;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inloop.ding.logic.DingCore;
import eu.inloop.ding.logic.DingMessage;

@Blade
public final class MerchantSendActivity extends BaseActivity {

    @State
    String mAmount;
    StringBuilder mAmountBuilder;

    @BindView((R.id.amount_text))
    TextView mAmountText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_send);

        ButterKnife.bind(this);

        mAmountBuilder = new StringBuilder(mAmount != null ? mAmount : "");
        refreshAmount();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mAmount = mAmountBuilder.toString();
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

    @OnClick(R.id.send_button)
    public void onSendClick(View view) {
        final String key = UUID.randomUUID().toString();
        final float price = Float.parseFloat(mAmountText.getText().toString());
        final String desc = "some info";
        final DingMessage dingMessage = new DingMessage(key, price, desc);

        DingCore.getInstance(getApplicationContext())
                .send(dingMessage);

        I.startMerchantAwaitPaymentActivity(this, key);
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
            return; // prxoffTODO
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
