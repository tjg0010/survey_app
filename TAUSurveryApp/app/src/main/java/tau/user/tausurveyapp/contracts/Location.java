package tau.user.tausurveyapp.contracts;

import com.google.gson.annotations.Expose;

public class Location {
    @Expose
    public String latitude;

    @Expose
    public String longitude;

    @Expose
    public long time;

    public Location() {
    }

    public Location(String latitude, String longitude, long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
}
