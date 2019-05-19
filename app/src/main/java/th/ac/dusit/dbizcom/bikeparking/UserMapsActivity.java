package th.ac.dusit.dbizcom.bikeparking;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Retrofit;
import th.ac.dusit.dbizcom.bikeparking.etc.MyPrefs;
import th.ac.dusit.dbizcom.bikeparking.etc.Utils;
import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;
import th.ac.dusit.dbizcom.bikeparking.model.User;
import th.ac.dusit.dbizcom.bikeparking.net.AddParkingPlaceResponse;
import th.ac.dusit.dbizcom.bikeparking.net.ApiClient;
import th.ac.dusit.dbizcom.bikeparking.net.GetParkingPlaceResponse;
import th.ac.dusit.dbizcom.bikeparking.net.MyRetrofitCallback;
import th.ac.dusit.dbizcom.bikeparking.net.WebServices;

public class UserMapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private List<ParkingPlace> mParkingPlaceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        TextView nameTextView = navigationView.getHeaderView(0).findViewById(R.id.name_text_view);
        User user = MyPrefs.getUserPref(this);
        nameTextView.setText(user.firstName + " " + user.lastName);

        TextView pidTextView = navigationView.getHeaderView(0).findViewById(R.id.pid_text_view);
        pidTextView.setText(user.pid);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        doGetParkingPlace();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_log_out) {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("ยืนยันออกจากระบบ?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MyPrefs.setUserPref(UserMapsActivity.this, null);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void doGetParkingPlace() {

        //mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        Call<GetParkingPlaceResponse> call = services.getParkingPlace();
        call.enqueue(new MyRetrofitCallback<>(
                UserMapsActivity.this,
                null,
                null,
                new MyRetrofitCallback.MyRetrofitCallbackListener<GetParkingPlaceResponse>() {
                    @Override
                    public void onSuccess(GetParkingPlaceResponse responseBody) {
                        mParkingPlaceList = responseBody.parkingPlaceList;

                        for (ParkingPlace place : mParkingPlaceList) {
                            double lat = Double.parseDouble(place.latitude);
                            double lng = Double.parseDouble(place.longitude);

                            LatLng position = new LatLng(lat, lng);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(place.district));
                            marker.setTag(place);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
                        }

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                findViewById(R.id.book_layout).setVisibility(View.VISIBLE);

                                final ParkingPlace place = (ParkingPlace) marker.getTag();

                                TextView detailsTextView = findViewById(R.id.details_text_view);
                                String msg = String.format(
                                        Locale.getDefault(),
                                        "%s\nชื่อผู้ให้บริการ: %s\nจำนวนที่จอด: %d\nราคา: %d\nหมายเหตุ: %s",
                                        place.placeName,
                                        place.firstName + " " + place.lastName,
                                        place.lotCount,
                                        place.fee,
                                        place.remark
                                );
                                detailsTextView.setText(msg);

                                findViewById(R.id.book_button).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        doBooking(place);
                                    }
                                });

                                return false;
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) { // เกิดข้อผิดพลาด (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(UserMapsActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }

    private void doBooking(final ParkingPlace place) {
        doAddBooking(place);
    }

    private void doAddBooking(final ParkingPlace place) {
        //mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        User user = MyPrefs.getUserPref(UserMapsActivity.this);

        Call<AddParkingPlaceResponse> call = services.addBooking(user.id, place.id);
        call.enqueue(new MyRetrofitCallback<>(
                UserMapsActivity.this,
                null,
                null,
                new MyRetrofitCallback.MyRetrofitCallbackListener<AddParkingPlaceResponse>() {
                    @Override
                    public void onSuccess(AddParkingPlaceResponse responseBody) {
                        Utils.showShortToast(UserMapsActivity.this, responseBody.errorMessage);

                        new AlertDialog.Builder(UserMapsActivity.this)
                                .setMessage("ต้องการใช้ Google Maps นำทางเดี๋ยวนี้หรือไม่")
                                .setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Uri intentUri = Uri.parse("geo:" + place.latitude + "," + place.longitude + "?q=" + place.latitude + "," + place.longitude);
                                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                                        mapIntent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapIntent);
                                    }
                                })
                                .setNegativeButton("ยกเลิก", null)
                                .show();
                    }

                    @Override
                    public void onError(String errorMessage) { // เกิดข้อผิดพลาด (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(UserMapsActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }
}
