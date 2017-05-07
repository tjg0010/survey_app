package tau.user.tausurveyapp.contracts;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;

public class Address {
    @Expose
    private String street;
    @Expose
    private Integer number;
    @Expose
    private String city;

    public Address(String streetName, Integer streetNumber, String city) {
        this.street = streetName;
        this.number = streetNumber;
        this.city = city;
    }

    public boolean isEmpty(String defaultValue) {
        return TextUtils.isEmpty(this.street) || this.number == null || TextUtils.isEmpty(this.city) || this.city.equalsIgnoreCase(defaultValue);
    }
}
