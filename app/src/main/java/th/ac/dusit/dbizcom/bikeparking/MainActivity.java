package th.ac.dusit.dbizcom.bikeparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import th.ac.dusit.dbizcom.bikeparking.etc.MyPrefs;
import th.ac.dusit.dbizcom.bikeparking.model.User;

public class MainActivity extends AppCompatActivity {

    public static final int ROLE_USER = 0;
    public static final int ROLE_PROVIDER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button userButton = findViewById(R.id.user_button);
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = MyPrefs.getUserPref(MainActivity.this);
                Intent intent = user == null ?
                        (new Intent(MainActivity.this, LoginActivity.class)) :
                        (new Intent(MainActivity.this, UserMapsActivity.class));
                intent.putExtra("role", ROLE_USER);
                startActivity(intent);
            }
        });
        Button providerButton = findViewById(R.id.provider_button);
        providerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User provider = MyPrefs.getProviderPref(MainActivity.this);
                Intent intent = provider == null ?
                        (new Intent(MainActivity.this, LoginActivity.class)) :
                        (new Intent(MainActivity.this, ProviderMainActivity.class));
                intent.putExtra("role", ROLE_PROVIDER);
                startActivity(intent);
            }
        });
    }
}
