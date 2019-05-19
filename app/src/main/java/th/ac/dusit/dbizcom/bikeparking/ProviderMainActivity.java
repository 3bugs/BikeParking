package th.ac.dusit.dbizcom.bikeparking;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import th.ac.dusit.dbizcom.bikeparking.adapter.ParkingPlaceListAdapter;
import th.ac.dusit.dbizcom.bikeparking.etc.MyPrefs;
import th.ac.dusit.dbizcom.bikeparking.etc.Utils;
import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;
import th.ac.dusit.dbizcom.bikeparking.model.User;
import th.ac.dusit.dbizcom.bikeparking.net.ApiClient;
import th.ac.dusit.dbizcom.bikeparking.net.GetParkingPlaceResponse;
import th.ac.dusit.dbizcom.bikeparking.net.MyRetrofitCallback;
import th.ac.dusit.dbizcom.bikeparking.net.WebServices;

public class ProviderMainActivity extends AppCompatActivity
        implements ParkingPlaceListAdapter.OnMenuItemClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private ListView mParkingPlaceListView;
    private View mProgressView;
    private List<ParkingPlace> mParkingPlaceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_2);

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
        User user = MyPrefs.getProviderPref(this);
        nameTextView.setText(user.firstName + " " + user.lastName);

        TextView pidTextView = navigationView.getHeaderView(0).findViewById(R.id.pid_text_view);
        pidTextView.setText(user.pid);

        Button addParkingPlaceButton = findViewById(R.id.add_parking_place_button);
        addParkingPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProviderMainActivity.this, ProviderAddParkingPlaceActivity.class);
                startActivity(intent);
            }
        });

        mProgressView = findViewById(R.id.progress_view);
        mParkingPlaceListView = findViewById(R.id.parking_place_list_view);
        mParkingPlaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProviderMainActivity.this, ProviderBookingListActivity.class);
                ParkingPlace p = mParkingPlaceList.get(position);
                intent.putExtra("parking_place", new Gson().toJson(p));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        doGetParkingPlace();
    }

    private void doGetParkingPlace() {

        mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        User provider = MyPrefs.getProviderPref(ProviderMainActivity.this);

        Call<GetParkingPlaceResponse> call = services.getParkingPlaceProvider(provider.id);
        call.enqueue(new MyRetrofitCallback<>(
                ProviderMainActivity.this,
                null,
                mProgressView,
                new MyRetrofitCallback.MyRetrofitCallbackListener<GetParkingPlaceResponse>() {
                    @Override
                    public void onSuccess(GetParkingPlaceResponse responseBody) {
                        mParkingPlaceList = responseBody.parkingPlaceList;

                        Utils.showLongToast(ProviderMainActivity.this, String.valueOf(mParkingPlaceList.size()));

                        ParkingPlaceListAdapter adapter = new ParkingPlaceListAdapter(
                                ProviderMainActivity.this,
                                R.layout.item_parking_place,
                                mParkingPlaceList,
                                ProviderMainActivity.this
                        );

                        mParkingPlaceListView.setAdapter(adapter);
                    }

                    @Override
                    public void onError(String errorMessage) { // เกิดข้อผิดพลาด (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(ProviderMainActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }

    @Override
    public void onClickEdit(ParkingPlace parkingPlace) {
        Intent intent = new Intent(ProviderMainActivity.this, ProviderAddParkingPlaceActivity.class);
        String json = new Gson().toJson(parkingPlace);
        intent.putExtra("parking_place", json);
        startActivity(intent);
    }

    @Override
    public void onClickDelete(ParkingPlace parkingPlace) {
        doDeleteParkingPlace(parkingPlace);
    }

    private void doDeleteParkingPlace(ParkingPlace parkingPlace) {
        mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        Call<GetParkingPlaceResponse> call = services.deleteParkingPlace(parkingPlace.id);
        call.enqueue(new MyRetrofitCallback<>(
                ProviderMainActivity.this,
                null,
                mProgressView,
                new MyRetrofitCallback.MyRetrofitCallbackListener<GetParkingPlaceResponse>() {
                    @Override
                    public void onSuccess(GetParkingPlaceResponse responseBody) {
                        Utils.showShortToast(ProviderMainActivity.this, responseBody.errorMessage);
                        doGetParkingPlace();
                    }

                    @Override
                    public void onError(String errorMessage) { // เกิดข้อผิดพลาด (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(ProviderMainActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.temp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                doGetParkingPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                            MyPrefs.setProviderPref(ProviderMainActivity.this, null);
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
}
