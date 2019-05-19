package th.ac.dusit.dbizcom.bikeparking.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    public final int id;
    @SerializedName("pid")
    public final String pid;
    @SerializedName("first_name")
    public final String firstName;
    @SerializedName("last_name")
    public final String lastName;

    public User(int id, String pid, String firstName, String lastName) {
        this.id = id;
        this.pid = pid;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
