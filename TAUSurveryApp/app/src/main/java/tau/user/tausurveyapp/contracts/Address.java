package tau.user.tausurveyapp.contracts;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;

public class Address {
    @Expose
    public String streetName;
    @Expose
    public String streetNumber;
    @Expose
    public String city;

    public Address() {

    }

    public Address(String streetName, String streetNumber, String city) {
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.city = city;
    }

    public boolean isEmpty(String defaultValue) {
        return TextUtils.isEmpty(this.streetName) || TextUtils.isEmpty(this.streetNumber) || TextUtils.isEmpty(this.city) || this.city.equalsIgnoreCase(defaultValue);
    }
}
