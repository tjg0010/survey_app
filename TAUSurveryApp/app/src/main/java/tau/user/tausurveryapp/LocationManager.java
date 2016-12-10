package tau.user.tausurveryapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

/**
 * Created by ran on 07/12/2016.
 */

public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private LocationManager ourInstance = null;
    private GoogleApiClient mGoogleApiClient = null;

    private ArrayList<LocationCallbackable> callbacks;
    private Context currentContext;

    public LocationManager() {
        callbacks = new ArrayList<>();
    }

    public void VerifyLocationSettings() {
        // Add this request via the LocationSettingsRequest builder.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest());

        // Make sure the user's settings are satisfied.
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(LocationSettingsResult result) {
//                final Status status = result.getStatus();
//                //final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        // All location settings are satisfied. The client can
//                        // initialize location requests here.
//                        break;
//                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        // Location settings are not satisfied, but this can be fixed
//                        // by showing the user a dialog.
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
//                        }
//                        catch (IntentSender.SendIntentException e) {
//                            // Ignore the error.
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        // Location settings are not satisfied. However, we have no way
//                        // to fix the settings so we won't show the dialog.
//                        break;
//                }
//            }
//        });
    }

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

        VerifyLocationSettings();

        // Connect to google api client.
        // When connected, this will trigger onConnected and then we'll be able to request the user's location.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Create a location request.
        LocationRequest locationRequest = createLocationRequest();

        // Check if we have permissions to get fine location.
        if (ActivityCompat.checkSelfPermission(currentContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permissions, launch LocationPermissionActivity. It will ask the user for permissions.
            // But we can only do it if we have the current context.
            if (currentContext != null) {
                Intent intent = new Intent(currentContext, LocationPermissionActivity.class);
                currentContext.startActivity(intent);
            }
            else {
                Log.e("LocationManager", "Tried using currentContext but it was null!");
            }

            // Call the given callback with null.
            runCallback(null);
            return;
        }

        // We have permissions :)
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            // Call the callback that the caller has supplied us with.
            runCallback(location);
            // Disconnect from GoogleApiClient.
            disconnect();
        }
        // Location was null - that means the GoogleApiClient didn't have enough time to get the current location.
        else
        {
            // In that case, we register ourselves to getLocationUpdates and unregister after we get the location.
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    runCallback(location);
                    disconnect();
                }
            });
        }
    }

    private LocationRequest createLocationRequest() {
        // Create a location request that sets all the settings we need.
        final LocationRequest locationRequest = new LocationRequest();
        // Set the interval in which we want to receive the location.
        locationRequest.setInterval(10000);
        // Set the fastest interval we agree to accept. We don't care much because we only need one update.
        locationRequest.setFastestInterval(1000);
        // Tell that we want a very accurate location.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set that we only want one update.
        locationRequest.setNumUpdates(1);
        // If we don't get an update, kill the request after 30 seconds.
        locationRequest.setExpirationDuration(30000);

        return locationRequest;
    }

    private void runCallback(Location location) {
        // Only do something if we have a callback registered.
        if (!callbacks.isEmpty()) {
            LocationCallbackable callback = callbacks.remove(0);

            if (location != null) {
                callback.run(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            }
            else {
                callback.run(null, null);
            }
        }
        // This shouldn't happen, but if it does, log it as an error.
        else
        {
            Log.e("TrackingService", "callbacks list was empty but a callback call was made");
        }

    }

    /** This function disconnects from the GoogleApiClient. */
    private void disconnect() {
        // Disconnect from google api client.
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LocationManager", "GoogleApiClient connection was suspended, connecting again...");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("LocationManager", "Connection failed. Error code: " + connectionResult.getErrorCode() + ". Error Message: " + connectionResult.getErrorMessage());
    }

    /** Interface for supplying a callback for a location request from the location manager. */
    public interface LocationCallbackable {
        void run(String latitude, String longitude);
    }
}


