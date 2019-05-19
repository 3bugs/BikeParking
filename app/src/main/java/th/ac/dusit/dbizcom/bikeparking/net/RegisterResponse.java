package th.ac.dusit.dbizcom.bikeparking.net;

import com.google.gson.annotations.SerializedName;

import th.ac.dusit.dbizcom.bikeparking.model.User;

public class RegisterResponse extends BaseResponse {

    @SerializedName("user")
    public User user;
}
