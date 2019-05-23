package th.ac.dusit.dbizcom.bikeparking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import th.ac.dusit.dbizcom.bikeparking.R;
import th.ac.dusit.dbizcom.bikeparking.model.Booking;

public class BookingListAdapter extends ArrayAdapter<Booking> {

    private Context mContext;
    private int mResourceId;
    private List<Booking> mBookingList;
    private OnMenuItemClickListener mListener;

    public BookingListAdapter(Context context, int resource,
                              List<Booking> bookingList,
                              OnMenuItemClickListener listener) {
        super(context, resource, bookingList);

        mContext = context;
        mResourceId = resource;
        mBookingList = bookingList;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mBookingList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(
                    mResourceId, parent, false
            );
        }

        final Booking booking = mBookingList.get(position);

        String buttonText = "";
        boolean showButton = true;
        switch (booking.status) {
            case 0:
                v.setBackgroundResource(R.color.booking_status_0);
                buttonText = "ยืนยันการจอง";
                break;
            case 1:
                v.setBackgroundResource(R.color.booking_status_1);
                buttonText = "ยกเลิกการจอด";
                break;
            case 2:
                v.setBackgroundResource(R.color.booking_status_2);
                showButton = false;
                break;
        }

        TextView userNameTextView = v.findViewById(R.id.user_name_text_view);
        String userName = booking.userFirstName + " " + booking.userLastName;
        userNameTextView.setText(userName);

        TextView pidTextView = v.findViewById(R.id.pid_text_view);
        String msg = String.format(
                Locale.getDefault(),
                "เลขประชาชน: %s\nเบอร์โทร: %s",
                booking.userPid, booking.userPhone
        );
        pidTextView.setText(msg);

        Button acceptBookingButton = v.findViewById(R.id.accept_booking_button);
        acceptBookingButton.setText(buttonText);
        acceptBookingButton.setVisibility(showButton ? View.VISIBLE : View.GONE);
        acceptBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onClickAcceptButton(booking);
                }
            }
        });

        /*final ImageView moreImageView = v.findViewById(R.id.more_image_view);
        moreImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, moreImageView);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_edit:
                                if (mListener != null) {
                                    mListener.onClickEdit(booking);
                                }
                                return true;
                            case R.id.action_delete:
                                if (mListener != null) {
                                    mListener.onClickDelete(booking);
                                }
                                return true;
                        }
                        return false;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.parking_place, popup.getMenu());
                popup.show();
            }
        });*/

        return v;
    }

    public interface OnMenuItemClickListener {
        void onClickAcceptButton(Booking booking);
    }
}
