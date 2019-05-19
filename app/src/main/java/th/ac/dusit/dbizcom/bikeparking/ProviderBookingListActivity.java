package th.ac.dusit.dbizcom.bikeparking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;
import th.ac.dusit.dbizcom.bikeparking.adapter.BookingListAdapter;
import th.ac.dusit.dbizcom.bikeparking.etc.Utils;
import th.ac.dusit.dbizcom.bikeparking.model.Booking;
import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;
import th.ac.dusit.dbizcom.bikeparking.net.AddParkingPlaceResponse;
import th.ac.dusit.dbizcom.bikeparking.net.ApiClient;
import th.ac.dusit.dbizcom.bikeparking.net.MyRetrofitCallback;
import th.ac.dusit.dbizcom.bikeparking.net.WebServices;

public class ProviderBookingListActivity extends AppCompatActivity {

    private ParkingPlace mParkingPlace;
    private BookingListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_booking_list);

        Intent intent = getIntent();
        String json = intent.getStringExtra("parking_place");
        mParkingPlace = new Gson().fromJson(json, ParkingPlace.class);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("ที่จอด: " + mParkingPlace.placeName);

        setupListView();
    }

    private void setupListView() {
        ListView bookingListView = findViewById(R.id.booking_list_view);

        mAdapter = new BookingListAdapter(
                this,
                R.layout.item_booking,
                mParkingPlace.bookingList,
                new BookingListAdapter.OnMenuItemClickListener() {
                    @Override
                    public void onClickAcceptButton(Booking booking) {
                        switch (booking.status) {
                            case 0:
                                doUpdateBooking(booking, 1);
                                break;
                            case 1:
                                doUpdateBooking(booking, 2);
                                break;
                            case 2:
                                break;
                        }
                    }
                }
        );

        bookingListView.setAdapter(mAdapter);
    }

    private void doUpdateBooking(final Booking booking, final int status) {
        //mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        Call<AddParkingPlaceResponse> call = services.updateBooking(booking.id, status);
        call.enqueue(new MyRetrofitCallback<>(
                ProviderBookingListActivity.this,
                null,
                null,
                new MyRetrofitCallback.MyRetrofitCallbackListener<AddParkingPlaceResponse>() {
                    @Override
                    public void onSuccess(AddParkingPlaceResponse responseBody) {
                        Utils.showShortToast(ProviderBookingListActivity.this, responseBody.errorMessage);
                        booking.status = status;
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String errorMessage) { // เกิดข้อผิดพลาด (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(ProviderBookingListActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }
}
