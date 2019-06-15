package th.ac.dusit.dbizcom.bikeparking.net;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;
import th.ac.dusit.dbizcom.bikeparking.model.Slip;

public class GetSlipResponse extends BaseResponse {

    @SerializedName("data_list")
    public List<Slip> slipList;
}
