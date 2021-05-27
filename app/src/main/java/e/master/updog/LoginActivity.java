package e.master.updog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login = findViewById(R.id.loginbtn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText channelName = findViewById(R.id.mychannel_name);
                if( TextUtils.isEmpty(channelName.getText())){
                    Toast.makeText(LoginActivity.this,"Channel name is required!",Toast.LENGTH_SHORT).show();

                    channelName.setError( "Channel name is required!" );

                }else {
                    Intent main = new Intent(LoginActivity.this, MainActivity.class);

                    main.putExtra("channelName", channelName.getText().toString());
                    startActivity(main);
                }
            }
        });
    }
}