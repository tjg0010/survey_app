package tau.user.tausurveyapp.contracts;

import com.google.gson.annotations.Expose;

public class BluetoothDeviceData {
    @Expose
    public String name;

    @Expose
    public String mac;

    @Expose
    public String type;

    @Expose
    public long time;

    public BluetoothDeviceData(String name, String mac, String type, long time) {
        this.name = name;
        this.mac = mac;
        this.type = type;
        this.time = time;
    }
}
