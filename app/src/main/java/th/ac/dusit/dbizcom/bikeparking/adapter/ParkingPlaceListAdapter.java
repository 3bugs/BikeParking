package th.ac.dusit.dbizcom.bikeparking.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import th.ac.dusit.dbizcom.bikeparking.R;
import th.ac.dusit.dbizcom.bikeparking.model.Booking;
import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;

public class ParkingPlaceListAdapter extends ArrayAdapter<ParkingPlace> {

    private Context mContext;
    private int mResourceId;
    private List<ParkingPlace> mParkingPlaceList;
    private OnMenuItemClickListener mListener;

    public ParkingPlaceListAdapter(Context context, int resource,
                                   List<ParkingPlace> parkingPlaceList,
                                   OnMenuItemClickListener listener) {
        super(context, resource, parkingPlaceList);

        mContext = context;
        mResourceId = resource;
        mParkingPlaceList = parkingPlaceList;
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mParkingPlaceList.size();
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

        final ParkingPlace parkingPlace = mParkingPlaceList.get(position);

        TextView placeNameTextView = v.findViewById(R.id.place_name_text_view);
        placeNameTextView.setText(parkingPlace.placeName);
        TextView detailsTextView = v.findViewById(R.id.details_text_view);
        String msg = String.format(
                Locale.getDefault(),
                "จำนวนที่จอด: %d, ค่าบริการ: %d",
                parkingPlace.lotCount, parkingPlace.fee
        );
        detailsTextView.setText(msg);

        TextView badgeTextView = v.findViewById(R.id.badge_text_view);

        int waitCount = 0;
        for (Booking booking : parkingPlace.bookingList) {
            if (booking.status == 0) {
                waitCount++;
            }
        }

        if (waitCount > 0) {
            badgeTextView.setText(String.valueOf(waitCount));
        } else {
            badgeTextView.setVisibility(View.GONE);
        }

        final ImageView moreImageView = v.findViewById(R.id.more_image_view);
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
                                    mListener.onClickEdit(parkingPlace);
                                }
                                return true;
                            case R.id.action_delete:
                                if (mListener != null) {
                                    mListener.onClickDelete(parkingPlace);
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
        });

        return v;
    }

    public interface OnMenuItemClickListener {
        void onClickEdit(ParkingPlace parkingPlace);
        void onClickDelete(ParkingPlace parkingPlace);
    }
}
