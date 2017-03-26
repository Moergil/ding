package eu.inoop.ding;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.listen_button)
    public void onListenInitiated(View view) {
//        Intent intent = new Intent(this, MerchantPaySend.class);
        Intent intent = new Intent(this, CustomerPayReceiveActivity.class);
        startActivity(intent);
    }
}
