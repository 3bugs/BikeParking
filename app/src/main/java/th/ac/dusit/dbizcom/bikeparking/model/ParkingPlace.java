package th.ac.dusit.dbizcom.bikeparking.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ParkingPlace {

    @SerializedName("id")
    public final int id;
    @SerializedName("place_name")
    public final String placeName;
    @SerializedName("latitude")
    public final String latitude;
    @SerializedName("longitude")
    public final String longitude;
    @SerializedName("district")
    public final String district;
    @SerializedName("provider_id")
    public final int providerId;
    @SerializedName("lot_count")
    public final int lotCount;
    @SerializedName("fee")
    public final int fee;
    @SerializedName("remark")
    public final String remark;
    @SerializedName("first_name")
    public final String firstName;
    @SerializedName("last_name")
    public final String lastName;
    @SerializedName("booking_list")
    public final List<Booking> bookingList;

    public ParkingPlace(int id, String placeName, String latitude, String longitude, String district, int providerId, int lotCount, int fee, String remark, String firstName, String lastName, List<Booking> bookingList) {
        this.id = id;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.district = district;
        this.providerId = providerId;
        this.lotCount = lotCount;
        this.fee = fee;
        this.remark = remark;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bookingList = bookingList;
    }

    @Override
    public String toString() {
        return placeName;
    }
}
