package eu.inloop.ding;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import blade.Blade;
import blade.I;
import butterknife.ButterKnife;
import butterknife.OnClick;


@Blade
public final class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.listen_button)
    public void onListenInitiated(View view) {
        I.startCustomerPayReceiveActivity(this);
    }
}
