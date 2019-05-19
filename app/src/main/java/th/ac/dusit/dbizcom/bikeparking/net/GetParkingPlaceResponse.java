package th.ac.dusit.dbizcom.bikeparking.net;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import th.ac.dusit.dbizcom.bikeparking.model.ParkingPlace;

public class GetParkingPlaceResponse extends BaseResponse {

    @SerializedName("data_list")
    public List<ParkingPlace> parkingPlaceList;
}
