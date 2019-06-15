package th.ac.dusit.dbizcom.bikeparking.net;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface WebServices {

    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> login(
            @Field("pid") String pid,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("login_provider")
    Call<LoginResponse> loginProvider(
            @Field("pid") String pid,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("register")
    Call<RegisterResponse> register(
            @Field("role") int role,
            @Field("pid") String pid,
            @Field("phone") String phone,
            @Field("password") String password,
            @Field("first_name") String firstName,
            @Field("last_name") String lastName
    );

    @GET("get_parking_place")
    Call<GetParkingPlaceResponse> getParkingPlace(
    );

    @FormUrlEncoded
    @POST("get_parking_place")
    Call<GetParkingPlaceResponse> getParkingPlaceProvider(
            @Field("provider_id") int providerId
    );

    @FormUrlEncoded
    @POST("delete_parking_place")
    Call<GetParkingPlaceResponse> deleteParkingPlace(
            @Field("parking_place_id") int parkingPlaceId
    );

    @FormUrlEncoded
    @POST("add_parking_place")
    Call<AddParkingPlaceResponse> addParkingPlace(
            @Field("provider_id") int providerId,
            @Field("place_name") String placeName,
            @Field("district") String district,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("lot_count") int lotCount,
            @Field("fee") int fee,
            @Field("remark") String remark
    );

    @FormUrlEncoded
    @POST("update_parking_place")
    Call<AddParkingPlaceResponse> updateParkingPlace(
            @Field("parking_place_id") int parkingPlaceId,
            @Field("place_name") String placeName,
            @Field("district") String district,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("lot_count") int lotCount,
            @Field("fee") int fee,
            @Field("remark") String remark
    );

    @FormUrlEncoded
    @POST("add_booking")
    Call<AddParkingPlaceResponse> addBooking(
            @Field("user_id") int userId,
            @Field("parking_place_id") int parkingPlaceId
    );

    @FormUrlEncoded
    @POST("update_booking")
    Call<AddParkingPlaceResponse> updateBooking(
            @Field("booking_id") int bookingId,
            @Field("status") int status
    );

    @FormUrlEncoded
    @POST("get_slip")
    Call<GetSlipResponse> getSlipProvider(
            @Field("provider_id") int providerId
    );

    @FormUrlEncoded
    @POST("get_slip")
    Call<GetSlipResponse> getSlipUser(
            @Field("user_id") int userId
    );
}