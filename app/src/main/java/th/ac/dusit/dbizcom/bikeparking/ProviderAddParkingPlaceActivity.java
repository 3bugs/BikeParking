package th.ac.dusit.dbizcom.bikeparking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;
import th.ac.dusit.dbizcom.bikeparking.etc.MyPrefs;
import th.ac.dusit.dbizcom.bikeparking.etc.Utils;
import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;
import th.ac.dusit.dbizcom.bikeparking.net.AddParkingPlaceResponse;
import th.ac.dusit.dbizcom.bikeparking.net.ApiClient;
import th.ac.dusit.dbizcom.bikeparking.net.MyRetrofitCallback;
import th.ac.dusit.dbizcom.bikeparking.net.WebServices;

import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
import static com.google.android.gms.common.ConnectionResult.SUCCESS;

public class ProviderAddParkingPlaceActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final int REQUEST_FINE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_GOOGLE_PLAY_UPDATE = 2;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private String mLatitude = null, mLongitude = null;

    private EditText mPlaceNameEditText, mDistrictEditText, mLotCountEditText;
    private EditText mFeeEditText, mRemarkEditText;

    private ParkingPlace mParkingPlace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_add_parking_place);

        Intent intent = getIntent();
        String json = intent.getStringExtra("parking_place");
        if (json != null) {
            mParkingPlace = new Gson().fromJson(json, ParkingPlace.class);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSION
            );
        } else { // Permission granted.
            if (checkGooglePlayServicesReady()) {
                main();
            }
        }

        setupViews();

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFormValid()) {
                    doAddParkingPlace();
                }
            }
        });
    }

    private void setupViews() {
        mPlaceNameEditText = findViewById(R.id.place_name_edit_text);
        mDistrictEditText = findViewById(R.id.district_edit_text);
        mLotCountEditText = findViewById(R.id.lot_count_edit_text);
        mFeeEditText = findViewById(R.id.fee_edit_text);
        mRemarkEditText = findViewById(R.id.remark_edit_text);

        if (mParkingPlace != null) {
            mPlaceNameEditText.setText(mParkingPlace.placeName);
            mDistrictEditText.setText(mParkingPlace.district);
            mLotCountEditText.setText(String.valueOf(mParkingPlace.lotCount));
            mFeeEditText.setText(String.valueOf(mParkingPlace.fee));
            mRemarkEditText.setText(mParkingPlace.remark);
        }
    }

    private void doAddParkingPlace() {
        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        Call<AddParkingPlaceResponse> call;
        if (mParkingPlace == null) {
            call = services.addParkingPlace(
                    MyPrefs.getProviderPref(ProviderAddParkingPlaceActivity.this).id,
                    mPlaceNameEditText.getText().toString().trim(),
                    mDistrictEditText.getText().toString().trim(),
                    mLatitude,
                    mLongitude,
                    Integer.parseInt(mLotCountEditText.getText().toString()),
                    Integer.parseInt(mFeeEditText.getText().toString()),
                    mRemarkEditText.getText().toString().trim()
            );
        } else {
            call = services.updateParkingPlace(
                    mParkingPlace.id,
                    mPlaceNameEditText.getText().toString().trim(),
                    mDistrictEditText.getText().toString().trim(),
                    mLatitude,
                    mLongitude,
                    Integer.parseInt(mLotCountEditText.getText().toString()),
                    Integer.parseInt(mFeeEditText.getText().toString()),
                    mRemarkEditText.getText().toString().trim()
            );
        }

        call.enqueue(new MyRetrofitCallback<>(
                ProviderAddParkingPlaceActivity.this,
                null,
                null,
                new MyRetrofitCallback.MyRetrofitCallbackListener<AddParkingPlaceResponse>() {
                    @Override
                    public void onSuccess(AddParkingPlaceResponse responseBody) { // register สำเร็จ
                        Utils.showShortToast(ProviderAddParkingPlaceActivity.this, responseBody.errorMessage);
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) { // register ไม่สำเร็จ หรือเกิดข้อผิดพลาดอื่นๆ (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(ProviderAddParkingPlaceActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }

    private boolean isFormValid() {
        boolean valid = true;

        if (mPlaceNameEditText.getText().toString().trim().isEmpty()) {
            mPlaceNameEditText.setError("กรอกชื่อสถานที่");
            valid = false;
        }
        if (mDistrictEditText.getText().toString().trim().isEmpty()) {
            mDistrictEditText.setError("กรอกเขต");
            valid = false;
        }
        if (mLotCountEditText.getText().toString().trim().isEmpty()) {
            mLotCountEditText.setError("กรอกจำนวนที่จอด");
            valid = false;
        }
        if (mFeeEditText.getText().toString().trim().isEmpty()) {
            mFeeEditText.setError("กรอกค่าบริการ");
            valid = false;
        }

        return valid;
    }

    private void main() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private boolean checkGooglePlayServicesReady() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        switch (resultCode) {
            case SUCCESS:
                return true;
            case SERVICE_MISSING:
            case SERVICE_VERSION_UPDATE_REQUIRED:
            case SERVICE_DISABLED:
                GoogleApiAvailability.getInstance().getErrorDialog(
                        this, resultCode, REQUEST_GOOGLE_PLAY_UPDATE
                ).show();
                return false;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_UPDATE:
                if (resultCode == RESULT_OK) {
                    main();
                } else {
                    String msg = "Google Play Services ไม่มีหรือไม่ได้อัพเดท แอพจึงไม่สามารถทำงานต่อได้";
                    Utils.showOkDialog(
                            ProviderAddParkingPlaceActivity.this,
                            "ผิดพลาด",
                            msg
                    );
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_FINE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if (checkGooglePlayServicesReady()) {
                        main();
                    }
                } else {
                    // permission denied
                    String msg = "แอพไม่ได้รับอนุญาตให้เข้าถึงตำแหน่ง จึงไม่สามารถทำงานต่อได้";
                    Utils.showOkDialog(
                            ProviderAddParkingPlaceActivity.this,
                            "ผิดพลาด",
                            msg
                    );
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newPosition = marker.getPosition();
                mLatitude = String.valueOf(newPosition.latitude);
                mLongitude = String.valueOf(newPosition.longitude);

                /*String msg = String.format(
                        Locale.getDefault(),
                        "Lat: %s, Lng: %s",
                        String.valueOf(newPosition.latitude),
                        String.valueOf(newPosition.longitude)
                );
                Utils.showShortToast(
                        ProviderAddParkingPlaceActivity.this,
                        msg
                );*/
            }
        });

        if (mParkingPlace == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                double lat = location.getLatitude();
                                double lng = location.getLongitude();

                                mLatitude = String.valueOf(lat);
                                mLongitude = String.valueOf(lng);

                                LatLng position = new LatLng(lat, lng);
                                Marker marker = mMap.addMarker(
                                        new MarkerOptions()
                                                .position(position)
                                                .draggable(true)
                                );
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String msg = "เกิดข้อผิดพลาดในการหาตำแหน่งปัจจุบันของคุณ";
                            Utils.showOkDialog(
                                    ProviderAddParkingPlaceActivity.this,
                                    "ผิดพลาด",
                                    msg
                            );
                        }
                    });
        } else {
            double lat = Double.parseDouble(mParkingPlace.latitude);
            double lng = Double.parseDouble(mParkingPlace.longitude);

            mLatitude = String.valueOf(lat);
            mLongitude = String.valueOf(lng);

            LatLng position = new LatLng(lat, lng);
            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .draggable(true)
            );
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
        }
    }
}
