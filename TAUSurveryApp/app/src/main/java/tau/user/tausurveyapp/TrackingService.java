package tau.user.tausurveyapp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.List;

import tau.user.tausurveyapp.contracts.Location;
import tau.user.tausurveyapp.types.NetworkCallback;

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
            // Only track if the survey time is not finished.
            if (!SurveyManager.getInstance().isSurveyFinished(this)) {
                track();
            }
            else {
                // If the survey time is finished, stop the repeated tracking.
                TrackingRepeater.getInstance().stopRepeatedTracking(this);
            }
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
            public void run(final String latitude, final String longitude) {
                // If we succeeded getting the user's current location.
                if (latitude != null && longitude != null) {
                    // Get the current time (so we know when the location was sampled).
                    final long time = new Date().getTime();

                    // If we have failed locations (location we failed to send to the server before.
                    if (haveFailedLocations()) {
                        // Get the saved locations and add our new one.
                        List<Location> locations = getSavedLocations();
                        locations.add(new Location(latitude, longitude, time));
                        // Send the info to the server to bulk save everything.
                        NetworkManager.getInstance().sendLocationsBulk(_this, locations, new NetworkCallback<String>() {
                            @Override
                            public void onResponse(String response, boolean isSuccessful) {
                                if (isSuccessful) {
                                    // If we succeeded, clear the saved locations list.
                                    clearSavedLocations();
                                }
                                else {
                                    // If we failed, add the current locations to the saved locations list too, for next time.
                                    saveFailedLocation(latitude, longitude, time);
                                }
                                stopSelf();
                            }

                            @Override
                            public void onFailure(String error) {
                                saveFailedLocation(latitude, longitude, time);
                                stopSelf();
                            }
                        });
                    }
                    // No failed locations to send. Only send the current one.
                    else {
                        // Send the location to the server.
                        NetworkManager.getInstance().sendLocation(_this, latitude, longitude, time,
                            new NetworkCallback<String>() {
                                @Override
                                public void onResponse(String response, boolean isSuccessful) {
                                    if (!isSuccessful) {
                                        saveFailedLocation(latitude, longitude, time);
                                    }
                                    stopSelf();
                                }

                                @Override
                                public void onFailure(String error) {
                                    saveFailedLocation(latitude, longitude, time);
                                    stopSelf();
                                }
                            });
                    }
                }
            }
        });
    }

    private void saveFailedLocation(String latitude, String longitude, long time) {
        // Get the existing locations.
        List<Location> existingLocations = getSavedLocations();
        // Add the new location.
        existingLocations.add(new Location(latitude, longitude, time));
        // Save the locations back to the prefs.
        Utils.setObjectListToPrefs(TrackingService.this, R.string.key_saved_locations, existingLocations);
    }

    private boolean haveFailedLocations() {
        List<Location> savedLocationsMap = Utils.geObjectListFromPrefs(Location.class, TrackingService.this, R.string.key_saved_locations);
        return !savedLocationsMap.isEmpty();
    }

    private List<Location> getSavedLocations() {
        return Utils.geObjectListFromPrefs(Location.class, TrackingService.this, R.string.key_saved_locations);
    }

    private void clearSavedLocations() {
        Utils.setObjectListToPrefs(TrackingService.this, R.string.key_saved_locations, null);
    }
}
