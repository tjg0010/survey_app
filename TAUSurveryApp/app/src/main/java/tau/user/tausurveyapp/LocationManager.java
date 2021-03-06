package tau.user.tausurveyapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

import tau.user.tausurveyapp.contracts.TauLocation;

public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final int MAX_LOCATION_WAIT_TIME = 120 * 1000; // 2 minutes as maximum wait time we allow.

    private GoogleApiClient mGoogleApiClient = null;

    private ArrayList<LocationCallbackable> callbacks;
    private Context currentContext;
    private boolean isLocationUpdated = false;

    public LocationManager() {
        callbacks = new ArrayList<>();
    }

    /**
     * First function in the location collection chain.
     * This function is called from outside whenever a client wants to get the user's current location.
     * @param context - the context withing we are running.
     * @param callback - a callback class to be called once the sampling chain is completed.
     */
    public void GetCurrentLocation(Context context, final LocationCallbackable callback) {
        // Save the context for onConnect to use it.
        currentContext = context;

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Add the given callback to the callback list. This will be used to return the location to whoever requested it.
        callbacks.add(callback);

        // Connect to GoogleApiClient. When connected, this will trigger onConnected and then we'll be able to request the user's location.
        mGoogleApiClient.connect();
    }

    /**
     * Fired when we are connected to the GoogleApiClient that can provide us with the user's current location.
     * This can only happen after mGoogleApiClient.connect().
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Create a location request.
        LocationRequest locationRequest = createLocationRequest();

        // Check if we don't have permissions to get fine location.
        if (ActivityCompat.checkSelfPermission(currentContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we no permissions were granted - call the given callback with null.
            runCallback(null);
            return;
        }

        // If we got here, we have permissions :)
        // Try to get the last known location of the user, and take it only if it's from the last minute (it can be very old).
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null && isLocationFresh(location)) {
            // Disconnect from GoogleApiClient.
            disconnect();
            // Call the callback that the caller has supplied us with, with the location we have.
            runCallback(location);
        }
        // Location was null - that means the GoogleApiClient didn't have enough time to get the current location.
        // Location was too old - we need to get a fresh one.
        else {
            // In that case we should check if location settings are valid, and if so register to locationUpdated.
            // If the settings are not valid, we will terminate.
            VerifyLocationSettingsAndContinue(locationRequest);
        }
    }

    /**
     * Verify if the device's settings are valid for location sampling.
     * If they are - register to get location updates and unregister once we got it.
     * If they are not - the sampling chain is terminated, returning null to the client.
     * @param locationRequest
     */
    private void VerifyLocationSettingsAndContinue(final LocationRequest locationRequest) {
        // Add this request via the LocationSettingsRequest builder.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        final LocationManager _this = this;

        // Make sure the user's settings are satisfied.
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        // Set a callback for the pending result.
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // All location settings are satisfied to request location updates.
                        // So we register ourselves to getLocationUpdates and unregister after we get the location.
                        //noinspection MissingPermission - we already check for permission in the prior function "onConnected".
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, _this);

                        // We can get stuck waiting for an update from FusedLocationApi.requestLocationUpdates.
                        // To solve this, we wait 10 seconds and terminate our self (if we haven't already finished).
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        // Only try to stop the operation if we didn't get a location update.
                                        if (!isLocationUpdated) {
                                            stopLocationUpdatesAndDisconnect();
                                            runCallback(null);
                                        }
                                    }
                                },
                                MAX_LOCATION_WAIT_TIME);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    default:
                        // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                        // Terminate the location sampling by calling the callback with null.
                        disconnect();
                        runCallback(null);
                        break;
                }
            }
        });
    }

    /**
     * Fired after registering ourselves to requestLocationUpdates, and a location update is available.
     * @param location - the current location of the user.
     */
    @Override
    public void onLocationChanged(Location location) {
        isLocationUpdated = true;

        // We got our location sample - unregister ourselves from requesting location updates and disconnect.
        stopLocationUpdatesAndDisconnect();

        // Call the callback with the location we just got.
        runCallback(location);
    }

    /**
     * Ask FusedLocationApi to stop getting location updates and disconnects from GoogleApiClient.
     */
    private void stopLocationUpdatesAndDisconnect() {
        // Unregister ourselves from requesting location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        // Disconnect from GoogleApiClient.
        disconnect();
    }

    /**
     * A function that created a location request with our wanted configurations.
     * @return a locationRequest.
     */
    private LocationRequest createLocationRequest() {
        // Create a location request that sets all the settings we need.
        final LocationRequest locationRequest = new LocationRequest();
        // Set the interval in which we want to receive the location.
        locationRequest.setInterval(4000);
        // Set the fastest interval we agree to accept. We don't care much because we only need one update.
        locationRequest.setFastestInterval(1000);
        // Tell that we want a very accurate location.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set that we only want one update.
        locationRequest.setNumUpdates(1);
        // If we don't get an update, kill the request after the max location wait time minus a second (so we beat the timer).
        locationRequest.setExpirationDuration(MAX_LOCATION_WAIT_TIME - 1000);

        return locationRequest;
    }

    /**
     * Runs a callback class from the callbacks list if any exist, and sends it the given location.
     * @param location - a location to send to the callback.
     */
    private void runCallback(Location location) {
        // Only do something if we have a callback registered.
        if (!callbacks.isEmpty()) {
            LocationCallbackable callback = callbacks.remove(0);

            if (location != null) {
                TauLocation tauLocation = new TauLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), location.getTime());
                callback.run(tauLocation);
            }
            else {
                callback.run(null);
            }
        }
        // This shouldn't happen, but if it does, log it as an error.
        else
        {
            Log.e("TrackingService", "callbacks list was empty but a callback call was made");
        }

    }

    /**
     * This function disconnects from the GoogleApiClient.
     */
    private void disconnect() {
        // Disconnect from google api client.
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Checks if the given location was sampled less than a minute ago.
     * @return true if the location was sampled less than a minute ago, false otherwise.
     */
    private boolean isLocationFresh(Location location) {
        // We divide the subtraction in a million because they are both in nano seconds.
        long difInMillis = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1000000;
        // If the diff is less than a minute (60 seconds - there are a 1000 millis in a second).
        if ((difInMillis / (60 * 1000)) <= 1) {
            return true;
        }
        return false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LocationManager", "GoogleApiClient connection was suspended, connecting again...");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("LocationManager", "Connection failed. Error code: " + connectionResult.getErrorCode() + ". Error Message: " + connectionResult.getErrorMessage());
        // Failed to connect, return false (as null) to the callback.
        runCallback(null);
    }

    /**
     * Interface for supplying a callback for a location request from the location manager.
     */
    public interface LocationCallbackable {
        void run(TauLocation location);
    }
}


