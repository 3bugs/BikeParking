package th.ac.dusit.dbizcom.bikeparking.net;

import com.google.gson.annotations.SerializedName;

import th.ac.dusit.dbizcom.bikeparking.model.User;

public class LoginResponse extends BaseResponse {

    @SerializedName("login_success")
    public boolean loginSuccess;
    @SerializedName("user")
    public User user;
}
