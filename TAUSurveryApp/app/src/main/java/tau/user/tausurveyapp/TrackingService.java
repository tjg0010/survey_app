package tau.user.tausurveyapp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * Gets the user's location and sends it to the server.
 * If fails, it saves it locally to send it later.
 */
public class TrackingService extends IntentService {
    /** A constructor that simply calls the base class constructor. */
    public TrackingService() {
        super("TrackingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            track();
        }
    }

    /**
     * Tracks the user by getting it's current location and sending it to the server.
     * In case network operation fails, the data is saved locally and re-sent in the next try.
     */
    private void track() {
        Log.i("TrackingService", "Tracking current location now...");

        final TrackingService _this = this;

        // Get the user location
        LocationManager locationManager = new LocationManager();
        locationManager.GetCurrentLocation(this, new LocationManager.LocationCallbackable() {
            @Override
            public void run(String latitude, String longitude) {
                if (latitude != null && longitude != null) {
                    // Send the location to the server.
                    //NetworkManager.getInstance().SendLocation(_this, latitude, longitude);
                }

                // Close the service since it's no longer needed.
                stopSelf();
            }
        });
    }
}
