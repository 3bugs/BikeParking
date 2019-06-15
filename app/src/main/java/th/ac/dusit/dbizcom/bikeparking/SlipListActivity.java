package th.ac.dusit.dbizcom.bikeparking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Retrofit;
import th.ac.dusit.dbizcom.bikeparking.adapter.ParkingPlaceListAdapter;
import th.ac.dusit.dbizcom.bikeparking.adapter.SlipListAdapter;
import th.ac.dusit.dbizcom.bikeparking.etc.MyPrefs;
import th.ac.dusit.dbizcom.bikeparking.etc.Utils;
import th.ac.dusit.dbizcom.bikeparking.model.Booking;
import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;
import th.ac.dusit.dbizcom.bikeparking.model.Slip;
import th.ac.dusit.dbizcom.bikeparking.model.User;
import th.ac.dusit.dbizcom.bikeparking.net.ApiClient;
import th.ac.dusit.dbizcom.bikeparking.net.GetParkingPlaceResponse;
import th.ac.dusit.dbizcom.bikeparking.net.GetSlipResponse;
import th.ac.dusit.dbizcom.bikeparking.net.MyRetrofitCallback;
import th.ac.dusit.dbizcom.bikeparking.net.WebServices;

public class SlipListActivity extends AppCompatActivity {

    public static final String KEY_USER_TYPE = "user_type";
    public static final int USER_TYPE_USER = 0;
    public static final int USER_TYPE_PROVIDER = 1;

    private ListView mSlipListView;
    private View mProgressView;
    private List<Slip> mSlipList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slip_list);

        mProgressView = findViewById(R.id.progress_view);
        mSlipListView = findViewById(R.id.slip_list_view);
        mSlipListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    showSlip(mSlipList.get(position));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showSlip(Slip slip) throws ParseException {
        Date payDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(slip.payDate);

        String msg = String.format(
                Locale.getDefault(),
                "ได้รับเงิน %d บาท\n\nจาก %s %s\n\nวันที่ %s, เวลา %s น.",
                slip.fee,
                slip.userFirstName, slip.userLastName,
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(payDate),
                new SimpleDateFormat("HH.mm", Locale.getDefault()).format(payDate)
        );
        Utils.showOkDialog(
                this,
                "สลิปการชำระเงิน",
                msg
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        doGetSlip();
    }

    private void doGetSlip() {
        mProgressView.setVisibility(View.VISIBLE);

        Retrofit retrofit = ApiClient.getClient();
        WebServices services = retrofit.create(WebServices.class);

        Intent intent = getIntent();
        int userType = intent.getIntExtra(KEY_USER_TYPE, -1);

        User user;
        Call<GetSlipResponse> call;
        if (userType == USER_TYPE_PROVIDER) {
            user = MyPrefs.getProviderPref(SlipListActivity.this);
            call = services.getSlipProvider(user.id);
        } else {
            user = MyPrefs.getUserPref(SlipListActivity.this);
            call = services.getSlipUser(user.id);
        }

        call.enqueue(new MyRetrofitCallback<>(
                SlipListActivity.this,
                null,
                mProgressView,
                new MyRetrofitCallback.MyRetrofitCallbackListener<GetSlipResponse>() {
                    @Override
                    public void onSuccess(GetSlipResponse responseBody) {
                        mSlipList = responseBody.slipList;

                        SlipListAdapter adapter = new SlipListAdapter(
                                SlipListActivity.this,
                                R.layout.item_slip,
                                mSlipList
                        );

                        mSlipListView.setAdapter(adapter);
                    }

                    @Override
                    public void onError(String errorMessage) { // เกิดข้อผิดพลาด (เช่น ไม่มีเน็ต, server ล่ม)
                        Utils.showOkDialog(SlipListActivity.this, "ผิดพลาด", errorMessage);
                    }
                }
        ));
    }
}
