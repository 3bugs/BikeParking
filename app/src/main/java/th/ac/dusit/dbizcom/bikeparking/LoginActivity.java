package th.ac.dusit.dbizcom.bikeparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import retrofit2.Call;
import retrofit2.Retrofit;
import th.ac.dusit.dbizcom.bikeparking.etc.MyPrefs;
import th.ac.dusit.dbizcom.bikeparking.etc.Utils;
import th.ac.dusit.dbizcom.bikeparking.net.ApiClient;
import th.ac.dusit.dbizcom.bikeparking.net.LoginResponse;
import th.ac.dusit.dbizcom.bikeparking.net.MyRetrofitCallback;
import th.ac.dusit.dbizcom.bikeparking.net.WebServices;

import static th.ac.dusit.dbizcom.bikeparking.MainActivity.ROLE_USER;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_REGISTER = 10001;
    static final String KEY_PID = "pid";

    private EditText mPidEditText, mPasswordEditText;
    private View mProgressView;

    private int mRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        mRole = intent.getIntExtra("role", -1);

        mPidEditText = findViewById(R.id.pid_edit_text);
        mPasswordEditText = findViewById(R.id.password_edit_text);
        mProgressView = findViewById(R.id.progress_view);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFormValid()) {
                    doLogin();
                } else {
                    Utils.showShortToast(LoginActivity.this, "กรอกข้อมูลให้ครบ");
                }
            }
        });

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("role", mRole);
                startActivityForResult(intent, REQUEST_REGISTER);
            }
        });

        Button skipButton = findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, UserMapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isFormValid() {
        boolean valid = true;

        if (mPasswordEditText.getText().toString().trim().length() == 0) {
            mPasswordEditText.setText("");
            mPasswordEditText.setError("กรอกรหัสผ่าน");
            valid = false;
        }
        if (mPidEditText.getText().toString().trim().length() == 0) {
            mPidEditText.setText("");
            mPidEditText.setError("กรอกเลขบัตรประชาชน");
            valid = false;
        }

        return valid;
    }

    private void doLogin() {
        String username = mPidEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        Call<LoginResponse> call;
        if (mRole == 0) {
            call = services.login(username, password);
        } else {
            call = services.loginProvider(username, password);
        }

        call.enqueue(new MyRetrofitCallback<>(
                LoginActivity.this,
                null,
                mProgressView,
                new MyRetrofitCallback.MyRetrofitCallbackListener<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse responseBody) {
                        if (responseBody.loginSuccess) { // login สำเร็จ
                            // จำว่า user login แล้ว
                            if (mRole == ROLE_USER) {
                                MyPrefs.setUserPref(LoginActivity.this, responseBody.user);
                            } else {
                                MyPrefs.setProviderPref(LoginActivity.this, responseBody.user);
                            }
                            // แสดง toast
                            Utils.showShortToast(LoginActivity.this, "เข้าสู่ระบบสำเร็จ");

                            // ไปหน้าหลัก
                            Intent intent = null;
                            if (mRole == ROLE_USER) {
                                intent = new Intent(LoginActivity.this, UserMapsActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, ProviderMainActivity.class);
                            }

                            startActivity(intent);
                            // ปิดหน้า login
                            finish();
                        } else { // login ไม่สำเร็จ
                            Utils.showOkDialog(LoginActivity.this, "เข้าสู่ระบบไม่สำเร็จ", "ชื่อผู้ใช้ หรือรหัสผ่าน ไม่ถูกต้อง");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) { // เกิดข้อผิดพลาด (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(LoginActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_REGISTER) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String username = data.getStringExtra(KEY_PID);
                    mPidEditText.setText(username);
                    mPasswordEditText.requestFocus();
                }
            }
        }
    }
}
