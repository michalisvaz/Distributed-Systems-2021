package e.master.updog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import e.master.updog.utilities.Utilities;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login = findViewById(R.id.loginbtn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText channelEditText = findViewById(R.id.mychannel_name);
                EditText brokerEditText = findViewById(R.id.brokertxt);
                if (TextUtils.isEmpty(channelEditText.getText())) { // if user didn't give a channel name
                    Toast.makeText(LoginActivity.this, "Channel name is required!", Toast.LENGTH_SHORT).show();
                    channelEditText.setError("Channel name is required!");
                } else if (TextUtils.isEmpty(brokerEditText.getText())) { // if user didn't give broker info
                    Toast.makeText(LoginActivity.this, "Broker info is required!", Toast.LENGTH_SHORT).show();
                    brokerEditText.setError("Broker info is required!");
                } else {
                    String channelName = channelEditText.getText().toString();
                    String brokerInfo = brokerEditText.getText().toString();
                    if (Utilities.checkBrokerInfo(brokerInfo)) { // if everything ok move on to the next(main) Activity
                        Intent main = new Intent(LoginActivity.this, MainActivity.class);
                        main.putExtra("channelName", channelName);
                        main.putExtra("brokerInfo", brokerInfo);
                        Log.d("LOGIN", "Opening main activity");
                        startActivity(main);
                    } else { // if broker info given is invalid
                        Toast.makeText(LoginActivity.this, "Wrong broker info format!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}