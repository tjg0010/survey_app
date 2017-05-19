package tau.user.tausurveyapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tau.user.tausurveyapp.contracts.DayOfWeek;
import tau.user.tausurveyapp.types.NotificationTime;


public class NotificationsManager {
    private static final NotificationsManager self = new NotificationsManager();

    private ArrayList<NotificationTime> defaultTimes;

    public static NotificationsManager getInstance() {
        return self;
    }

    private NotificationsManager() {
        defaultTimes = new ArrayList<>(2);
        // TODO: uncomment this and delete what's under!
        //defaultTimes.add(new NotificationTime(DayOfWeek.WEDNESDAY, 20, 0));
        //defaultTimes.add(new NotificationTime(DayOfWeek.SATURDAY, 21, 0));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 2);
        defaultTimes.add(new NotificationTime(DayOfWeek.FRIDAY, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
        defaultTimes.add(new NotificationTime(DayOfWeek.FRIDAY, 22, 30));
    }

    /**
     * Set the dates for which a notification will appear.
     * @param context - used to get resources.
     * @param times - the desired dates.
     */
    public void setDates(Context context, List<NotificationTime> times) {
        // Only set the dates if they haven't been set yet.
        if (!Utils.getBooleanFromPrefs(context, R.string.key_survey_notifications_saved)) {
            // Converts the times to a specific time in milliseconds. Use the default times if not supplied with requested times.
            List<Long> unixTimes = convertNotificationTimesToMillisDate((times == null || times.isEmpty()) ? this.defaultTimes : times);

            // Save the dates to the prefs (in case we will get booted and need to re-set the alarms).
            Utils.setStringListToPrefs(context, R.string.key_survey_notifications_times, Utils.convertToStringList(unixTimes));

            // Mark the dates as set.
            Utils.setBooleanToPrefs(context, R.string.key_survey_notifications_saved, true);
        }
    }

    public void activateNotifications(Context context) {
        // Only do something if the dates were saved.
        if (!Utils.getBooleanFromPrefs(context, R.string.key_survey_notifications_saved)) {
            // Get the dates from the preferences (notice that this list can be empty, since we delete dates for notifications that are done).
            ArrayList<Long> times = new ArrayList<>();
            List<String> timesPrefs = Utils.getStringListFromPrefs(context, R.string.key_survey_notifications_times);
            times.addAll(Utils.convertToLongList(timesPrefs));

            // Go over the notification times we found and set an alarm for each one (with different ids).
            for (long time: times) {
                // Create the intent and tell it to active the NotificationReceiver.
                Intent notificationIntent = new Intent(context, NotificationReceiver.class);
                // Create a pending intent with an id equals to the time (but in int).
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int)time, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // Create the alarm manager and give it the pending intent and time.
                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pendingIntent);
                }
                else {
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pendingIntent);
                }
            }
        }
    }

    @SuppressWarnings("WrongConstant")
    private List<Long> convertNotificationTimesToMillisDate(List<NotificationTime> times) {
        ArrayList<Long> milliTimes = new ArrayList<>();

        // Go over the received times.
        for (NotificationTime time: times) {
            // Find the next dayOfWeek requested (the next time in the calendar that day comes).
            Calendar cal = Calendar.getInstance();
            while (cal.get(Calendar.DAY_OF_WEEK) != time.dayOfWeek.getValue()) {
                cal.add(Calendar.DATE, 1);
            }
            // Once found, set the hour and the minute.
            cal.set(Calendar.HOUR_OF_DAY, time.hour);
            cal.set(Calendar.MINUTE, time.minute);
            cal.set(Calendar.SECOND, 0);
            // Add the time as long to the array.
            milliTimes.add(cal.getTimeInMillis());
        }

        return milliTimes;
    }
}


