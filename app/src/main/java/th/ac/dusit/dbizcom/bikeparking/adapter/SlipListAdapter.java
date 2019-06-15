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
import th.ac.dusit.dbizcom.bikeparking.model.Slip;

public class SlipListAdapter extends ArrayAdapter<Slip> {

    private Context mContext;
    private int mResourceId;
    private List<Slip> mSlipList;

    public SlipListAdapter(Context context, int resource,
                           List<Slip> slipList) {
        super(context, resource, slipList);

        mContext = context;
        mResourceId = resource;
        mSlipList = slipList;
    }

    @Override
    public int getCount() {
        return mSlipList.size();
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

        final Slip slip = mSlipList.get(position);

        TextView feeTextView = v.findViewById(R.id.fee_text_view);
        feeTextView.setText(
                String.format(Locale.getDefault(), "ค่าจอด %d บาท", slip.fee)
        );

        TextView nameTextView = v.findViewById(R.id.name_text_view);
        nameTextView.setText(
                String.format(Locale.getDefault(), "%s %s", slip.userFirstName, slip.userLastName)
        );

        TextView dateTextView = v.findViewById(R.id.date_text_view);
        dateTextView.setText(slip.payDate);

        return v;
    }
}
