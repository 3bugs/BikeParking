package th.ac.dusit.dbizcom.bikeparking.net;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyRetrofitCallback<T extends BaseResponse> implements Callback<T> {

    private static final String TAG = MyRetrofitCallback.class.getName();
    private static final String NETWORK_ERROR_MESSAGE = "เกิดข้อผิดพลาดในการเชื่อมต่อเครือข่าย";

    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private View mProgressView;
    private MyRetrofitCallbackListener<T> mListener;

    public MyRetrofitCallback(Activity activity,
                              ProgressDialog progressDialog,
                              View progressView,
                              MyRetrofitCallbackListener<T> listener) {
        this.mActivity = activity;
        this.mProgressDialog = progressDialog;
        this.mProgressView = progressView;
        mListener = listener;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mProgressView != null) {
            mProgressView.setVisibility(View.GONE);
        }

        if (response.isSuccessful()) {
            T responseBody = response.body();

            if (responseBody != null) {
                boolean success = responseBody.errorCode == 0;

                if (success) {
                    if (mListener != null) {
                        mListener.onSuccess(responseBody);
                    }
                } else {
                    String msg = responseBody.errorMessage;
                    if (mListener != null) {
                        mListener.onError(msg);
                    }
                }
            } else {
                if (mListener != null) {
                    mListener.onError(NETWORK_ERROR_MESSAGE);
                }
            }

        } else { // HTTP request failed
            if (mListener != null) {
                mListener.onError(NETWORK_ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mProgressView != null) {
            mProgressView.setVisibility(View.GONE);
        }
        if (mListener != null) {
            mListener.onError(NETWORK_ERROR_MESSAGE);
            Log.e(TAG, t.getMessage());
        }
    }

    public interface MyRetrofitCallbackListener<T extends BaseResponse> {
        void onSuccess(T responseBody);
        void onError(String errorMessage);
    }
}
