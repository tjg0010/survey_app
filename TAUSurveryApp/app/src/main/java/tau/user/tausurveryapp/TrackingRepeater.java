package tau.user.tausurveryapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

/**
 * Created by ran on 05/12/2016.
 */

public class TrackingRepeater {
    private static TrackingRepeater ourInstance = new TrackingRepeater();
    private AlarmManager alarmMgr;

    public static TrackingRepeater getInstance() {
        return ourInstance;
    }

    private TrackingRepeater() {

    }

    public void startRepeatedTracking(Context context) {
        // Create the intent that starts the TrackingService.
        Intent intent = new Intent(context, TrackingService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        // Start the our continuous alarm with the alarm manager.
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
    }
}
