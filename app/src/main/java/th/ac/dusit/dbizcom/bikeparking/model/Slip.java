package th.ac.dusit.dbizcom.bikeparking.model;

import com.google.gson.annotations.SerializedName;

public class Slip {

    @SerializedName("booking_id")
    public final int bookingId;
    @SerializedName("pay_date")
    public final String payDate;
    @SerializedName("user_first_name")
    public final String userFirstName;
    @SerializedName("user_last_name")
    public final String userLastName;
    @SerializedName("fee")
    public final int fee;

    public Slip(int bookingId, String payDate, String userFirstName, String userLastName, int fee) {
        this.bookingId = bookingId;
        this.payDate = payDate;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.fee = fee;
    }
}
