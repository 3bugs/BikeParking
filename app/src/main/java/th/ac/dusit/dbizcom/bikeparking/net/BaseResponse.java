package th.ac.dusit.dbizcom.bikeparking.net;

import com.google.gson.annotations.SerializedName;

class BaseResponse {

    @SerializedName("error_code")
    public int errorCode;
    @SerializedName("error_message")
    public String errorMessage;
    @SerializedName("error_message_more")
    public String errorMessageMore;
}