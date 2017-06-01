package tau.user.tausurveyapp;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tau.user.tausurveyapp.contracts.BluetoothDeviceData;
import tau.user.tausurveyapp.contracts.NotificationTime;
import tau.user.tausurveyapp.types.NetworkCallback;

import static android.content.ContentValues.TAG;

public class BluetoothSamplingManager {
    private static final BluetoothSamplingManager self = new BluetoothSamplingManager();

    public static BluetoothSamplingManager getInstance() {
        return self;
    }

    private BluetoothSamplingManager() {
    }

    /**
     * Set the dates in which we should trigger a bluetooth sample.
     * @param context - used to get resources.
     * @param times - the desired dates.
     */
    public void setDates(Context context, List<NotificationTime> times) {
        // Only set the dates if they haven't been set yet.
        if (!Utils.getBooleanFromPrefs(context, R.string.key_bluetooth_dates_saved)) {
            // Converts the times to a specific time in milliseconds. If no times were given, save the key as null.
            List<Long> unixTimes = Utils.convertNotificationTimesToMillisDate((times == null) ? new ArrayList<NotificationTime>() : times);

            // Save the dates to the prefs so we can later use them.
            Utils.setUniqueStringListToPrefs(context, R.string.key_bluetooth_sampling_times, Utils.convertToStringList(unixTimes));

            // Mark the dates as set.
            Utils.setBooleanToPrefs(context, R.string.key_bluetooth_dates_saved, true);
        }
    }

    public void activateNotifications(Context context) {
        // Only do something if the dates were saved.
        if (Utils.getBooleanFromPrefs(context, R.string.key_bluetooth_dates_saved)) {
            // Get the sampling dates from the prefs.
            ArrayList<Long> times = new ArrayList<>();
            List<String> timesPrefs = Utils.getUniqueStringListFromPrefs(context, R.string.key_bluetooth_sampling_times);
            times.addAll(Utils.convertToLongList(timesPrefs));

            // Go over the bluetooth sampling times we found and set an alarm for each one (with different unique ids).
            for (long time: times) {
                // Create the intent and tell it to activate the BluetoothReceiver with our custom action.
                Intent bluetoothIntent = new Intent(context, BluetoothReceiver.class);
                bluetoothIntent.setAction("tau.user.tausurveyapp.action.BLUETOOTH_SAMPLE");
                // Create a pending intent with an id equals to the time (but in int).
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int)time, bluetoothIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // Create the alarm manager and give it the pending intent and time.
                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
                else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            }
        }
    }

    /**
     * Goes over the existing bluetooth sampling times, and removes them from the list saved in the preferences.
     */
    public void removeOldSampleTimes(Context context) {
        // Only do something if the bluetooth dates were set.
        if (Utils.getBooleanFromPrefs(context, R.string.key_bluetooth_dates_saved)) {
            ArrayList<Long> newTimes = new ArrayList<>();

            // Get the sampling dates from the prefs.
            ArrayList<Long> times = new ArrayList<>();
            List<String> timesPrefs = Utils.getUniqueStringListFromPrefs(context, R.string.key_bluetooth_sampling_times);
            times.addAll(Utils.convertToLongList(timesPrefs));
            // Go over what we found (if any).
            for (long time: times) {
                // If the time is in the future, save it.
                if (time >= System.currentTimeMillis()) {
                    newTimes.add(time);
                } else {
                    Log.i(TAG, String.format("BluetoothSamplingManager: removing old bluetooth sampling time %d", time));
                }
            }

            // Replace the existing sampling times list in the prefs with the new and updated list.
            Utils.setUniqueStringListToPrefs(context, R.string.key_bluetooth_sampling_times, Utils.convertToStringList(newTimes));
        }
    }

    public void sendBluetoothDataToServer(final Context context, final List<BluetoothDeviceData> bluetoothDataList) {
        Log.i(TAG, "BluetoothSamplingManager: sendBluetoothDataToServer was called");
        // Get saved data from the prefs and send it too.
        List<BluetoothDeviceData> fromPrefs = Utils.getObjectListFromPrefs(BluetoothDeviceData.class, context, R.string.key_saved_bluetooth_data);
        bluetoothDataList.addAll(fromPrefs);

        // There's only point in sending to the server if the list is not empty.
        if (!bluetoothDataList.isEmpty()) {
            NetworkManager.getInstance().sendBluetoothData(context, bluetoothDataList, new NetworkCallback<String>() {
                @Override
                public void onResponse(String response, boolean isSuccessful) {
                    if (isSuccessful) {
                        // If the data was successfully sent, clear the saved bluetooth data.
                        Log.i(TAG, "BluetoothSamplingManager: sendBluetoothDataToServer was successful");
                        Utils.setObjectListToPrefs(context, R.string.key_saved_bluetooth_data, null);
                    }
                    else {
                        // If the post wasn't successful, save the data and send it next time (the location service sends it to the server if it sees any).
                        Log.i(TAG, "BluetoothSamplingManager: sendBluetoothDataToServer was not successful");
                        Utils.setObjectListToPrefs(context, R.string.key_saved_bluetooth_data, bluetoothDataList);
                    }
                }

                @Override
                public void onFailure(String error) {
                    // If the post wasn't successful, save the data and send it next time (the location service sends it to the server if it sees any).
                    Log.i(TAG, "BluetoothSamplingManager: sendBluetoothDataToServer has failed");
                    Utils.setObjectListToPrefs(context, R.string.key_saved_bluetooth_data, bluetoothDataList);
                }
            });
        }
    }

    public void sendSavedBluetoothData(Context context) {
        // Just call sendBluetoothDataToServer with an empty list. It also handles sending saved bluetooth data.
        sendBluetoothDataToServer(context, new ArrayList<BluetoothDeviceData>());
    }
}
