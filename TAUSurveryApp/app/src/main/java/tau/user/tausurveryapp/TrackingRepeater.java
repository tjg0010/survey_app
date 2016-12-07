package tau.user.tausurveryapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;

/**
 * Created by ran on 05/12/2016.
 */

public class TrackingRepeater {
    private static TrackingRepeater ourInstance = new TrackingRepeater();

    public static TrackingRepeater getInstance() {
        return ourInstance;
    }

    private TrackingRepeater() {

    }

    public void startRepeatedTracking(Context context, boolean isBooter) {
        // Only start the alarm manager (i.e the tracking service every x minutes) if we are not yet tracking.
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isTrackingStarted = prefs.getBoolean(context.getString(R.string.key_was_tracking_started), false);

        // Also start running the service in repeat if we haven't done so yet or if this is called after device reboot.
        if (!isTrackingStarted || isBooter) {
            // Create the intent that starts the TrackingService.
            Intent intent = new Intent(context, TrackingService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

            // Start the our continuous alarm with the alarm manager.
            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    5000, pendingIntent);
                    //AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
        }

        // If we didn't start tracking yet, enable the booter.
        if (!isTrackingStarted) {
            // Enable the alarm to start after device is booted.
            // From now on, the app will start the repeater after boot.
            ComponentName receiver = new ComponentName(context, BootReceiver.class);
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            // Now tracking is started, save that knowledge.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(context.getString(R.string.key_was_tracking_started), true);
            editor.commit();
        }
    }

}
