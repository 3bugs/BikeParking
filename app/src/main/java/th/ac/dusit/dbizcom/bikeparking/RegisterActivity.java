package th.ac.dusit.dbizcom.bikeparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import retrofit2.Call;
import retrofit2.Retrofit;
import th.ac.dusit.dbizcom.bikeparking.etc.Utils;
import th.ac.dusit.dbizcom.bikeparking.model.User;
import th.ac.dusit.dbizcom.bikeparking.net.ApiClient;
import th.ac.dusit.dbizcom.bikeparking.net.MyRetrofitCallback;
import th.ac.dusit.dbizcom.bikeparking.net.RegisterResponse;
import th.ac.dusit.dbizcom.bikeparking.net.WebServices;

import static th.ac.dusit.dbizcom.bikeparking.LoginActivity.KEY_PID;

public class RegisterActivity extends AppCompatActivity {

    private EditText mPidEditText, mPasswordEditText, mConfirmPasswordEditText;
    private EditText mPhoneEditText, mFirstNameEditText, mLastNameEditText;
    private View mProgressView;

    private int mRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        mRole = intent.getIntExtra("role", -1);

        mFirstNameEditText = findViewById(R.id.first_name_edit_text);
        mLastNameEditText = findViewById(R.id.last_name_edit_text);
        mPidEditText = findViewById(R.id.pid_edit_text);
        mPhoneEditText = findViewById(R.id.phone_edit_text);
        mPasswordEditText = findViewById(R.id.password_edit_text);
        mConfirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        mProgressView = findViewById(R.id.progress_view);

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFormValid()) {
                    Utils.hideKeyboard(RegisterActivity.this);
                    doRegister();
                } else {
                    Utils.showShortToast(RegisterActivity.this, "กรอกข้อมูลให้ครบถ้วนและถูกต้อง");
                }
            }
        });
    }

    private boolean isFormValid() {
        boolean valid = true;

        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();
        if (confirmPassword.length() == 0) {
            mConfirmPasswordEditText.setText("");
            mConfirmPasswordEditText.setError("กรอกรหัสผ่านอีกครั้งเพื่อยืนยัน");
            valid = false;
        }
        String password = mPasswordEditText.getText().toString().trim();
        if (password.length() == 0) {
            mPasswordEditText.setText("");
            mPasswordEditText.setError("กรอกรหัสผ่าน");
            valid = false;
        }
        if (password.length() > 0 && confirmPassword.length() > 0
                && !password.equals(confirmPassword)) {
            mConfirmPasswordEditText.setError("กรอกยืนยันรหัสผ่านให้ตรงกัน");
            valid = false;
        }
        String phone = mPhoneEditText.getText().toString().trim();
        if (phone.length() == 0) {
            mPhoneEditText.setText("");
            mPhoneEditText.setError("กรอกเบอร์โทรศัพท์");
            valid = false;
        }
        String pid = mPidEditText.getText().toString().trim();
        if (pid.length() != 13) {
            mPidEditText.setError("กรอกหมายเลขบัตรประชาชน 13 หลัก");
            valid = false;
        } else if (!Utils.isValidPid(pid)) {
            mPidEditText.setError("หมายเลขบัตรประชาชนไม่ถูกต้อง");
            valid = false;
        }
        String lastName = mLastNameEditText.getText().toString().trim();
        if (lastName.length() == 0) {
            mLastNameEditText.setText("");
            mLastNameEditText.setError("กรอกนามสกุล");
            valid = false;
        }
        String firstName = mFirstNameEditText.getText().toString().trim();
        if (firstName.length() == 0) {
            mFirstNameEditText.setText("");
            mFirstNameEditText.setError("กรอกชื่อ");
            valid = false;
        }

        return valid;
    }

    private void doRegister() {
        String pid = mPidEditText.getText().toString().trim();
        String phone = mPhoneEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        String firstName = mFirstNameEditText.getText().toString().trim();
        String lastName = mLastNameEditText.getText().toString().trim();

        mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        Call<RegisterResponse> call = services.register(
                mRole, pid, phone, password, firstName, lastName
        );
        call.enqueue(new MyRetrofitCallback<>(
                RegisterActivity.this,
                null,
                mProgressView,
                new MyRetrofitCallback.MyRetrofitCallbackListener<RegisterResponse>() {
                    @Override
                    public void onSuccess(RegisterResponse responseBody) { // register สำเร็จ
                        User user = responseBody.user;
                        // ส่ง username ที่ register สำเร็จ กลับไปแสดงในหน้า login
                        Intent intent = new Intent();
                        intent.putExtra(KEY_PID, user.pid);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) { // register ไม่สำเร็จ หรือเกิดข้อผิดพลาดอื่นๆ (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(RegisterActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }
}
