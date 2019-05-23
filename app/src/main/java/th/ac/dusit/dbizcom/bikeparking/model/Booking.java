package th.ac.dusit.dbizcom.bikeparking.model;

import com.google.gson.annotations.SerializedName;

public class Booking {

    @SerializedName("id")
    public final int id;
    @SerializedName("user_id")
    public final int userId;
    @SerializedName("user_first_name")
    public final String userFirstName;
    @SerializedName("user_last_name")
    public final String userLastName;
    @SerializedName("user_pid")
    public final String userPid;
    @SerializedName("user_phone")
    public final String userPhone;
    @SerializedName("parking_place_id")
    public final int parkingPlaceId;
    @SerializedName("book_date")
    public final String bookDate;
    @SerializedName("status")
    public int status;

    public Booking(int id, int userId, String userFirstName, String userLastName,
                   String userPid, String userPhone, int parkingPlaceId, String bookDate, int status) {
        this.id = id;
        this.userId = userId;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userPid = userPid;
        this.userPhone = userPhone;
        this.parkingPlaceId = parkingPlaceId;
        this.bookDate = bookDate;
        this.status = status;
    }
}
