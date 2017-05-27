package tau.user.tausurveyapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;
import java.util.List;

import tau.user.tausurveyapp.activities.DiaryActivity;

public class NotificationReceiver extends BroadcastReceiver {

    private final int notificationId = 101;
    private final int snoozeIntentId = 102;
    private final int maxSnoozes = 3;
    private final int snoozeTime = 30; // 30 minutes snooze time.
    private final int snoozeTimeMorning = 9; // Set the morning snooze to 9AM;

    public static String IS_SNOOZE_CLICKED_ID = "is_snooze_clicked_id";

    @Override
    // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
    public void onReceive(Context context, Intent intent) {
        boolean isSnoozing = intent.getBooleanExtra(IS_SNOOZE_CLICKED_ID, false);
        int snoozeCount = Utils.getIntFromPrefs(context, R.string.key_survey_notifications_snooze_count);

        // Remove the notification from display (if any).
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        // If this receiver was called to raise an alarm (and not called because the snooze was clicked).
        if (!isSnoozing) {
            // Show notification.
            showNotification(context, snoozeCount);

            // Remove old notification time from preferences.
            List<Long> times = removeOldNotificationTime(context);

            // If we still want to alert the user via a snooze.
            if (snoozeCount <= maxSnoozes) {
                // Add an auto snooze notification time to preferences.
                if (snoozeCount == maxSnoozes) {
                    // Snooze till the morning.
                    times.add(Utils.getFutureMillisTimeByHourInDay(snoozeTimeMorning));
                } else {
                    // Snooze the snooze time amount.
                    times.add(Utils.getFutureMillisTimeByMinutes(snoozeTime));
                }
                // Save the updated times to the prefs.
                Utils.setUniqueStringListToPrefs(context, R.string.key_survey_notifications_times, Utils.convertToStringList(times));

                // Increment auto snooze count in the preferences.
                snoozeCount++;
                Utils.setIntToPrefs(context, R.string.key_survey_notifications_snooze_count, snoozeCount);

                // Call activateNotifications to get the new auto snoozed time set with an alarm.
                // No need to worry, alarms that already existed will be overwritten.
                NotificationsManager.getInstance().activateNotifications(context);
            }
            else {
                // Don't add a snooze time to the preferences.
                // Reset the snooze count in the preferences for the next alarm (in some day).
                NotificationsManager.getInstance().resetNotificationsSnoozeCount(context);
            }
        }
        // This receiver was called because the user has clicked the snooze button.
        else {
            // We don't do anything other than that. The auto snooze time will elapse anyway.
        }
    }

    private void showNotification(Context context, int snoozeCount) {
        long[] vibrationPattern = {500,500,500,500};
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                                                .setSmallIcon(R.mipmap.tau_launcher)
                                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.tau_launcher))
                                                .setContentTitle(context.getString(R.string.notification_title))
                                                .setContentText(context.getString(R.string.notification_body))
                                                .addAction(createOkAction(context)) // Create ok button.
                                                .setLights(Color.BLUE, 500, 500)
                                                .setVibrate(vibrationPattern)
                                                .setSound(notificationSound)
                                                .setPriority(Notification.PRIORITY_MAX); // So we get a heads-up notification when possible (when the user is active).

        // Add a snooze button if this is not the last snooze (if we still have at least one more snooze to go).
        if (snoozeCount <= maxSnoozes) {
            mBuilder.addAction(createSnoozeAction(context, snoozeCount));
        }

        // Creates an explicit intent for our DiaryActivity.
        Intent resultIntent = new Intent(context, DiaryActivity.class);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of the app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself).
        stackBuilder.addParentStack(DiaryActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows us to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    private NotificationCompat.Action createSnoozeAction(Context context, int snoozeCount) {
        // Create the intent and tell it to active the NotificationReceiver (we call ourselves but with a snooze flag).
        Intent snoozeIntent = new Intent(context, NotificationReceiver.class);
        snoozeIntent.putExtra(IS_SNOOZE_CLICKED_ID, true);
        // Create a pending intent with an id equals to the time (but in int).
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, snoozeIntentId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Adjust the text of the snooze button. If this is the last snooze, it suggest snoozing to the morning.
        int btnTextResId = snoozeCount == maxSnoozes ? R.string.notification_snooze_morning_btn : R.string.notification_snooze_btn;

        return new NotificationCompat.Action(
                R.drawable.tau_transparent_pixel,
                context.getString(btnTextResId),
                pendingIntent);
    }

    private NotificationCompat.Action createOkAction(Context context) {
        // Creates an explicit intent for our DiaryActivity.
        Intent resultIntent = new Intent(context, DiaryActivity.class);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of the app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself).
        stackBuilder.addParentStack(DiaryActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action(
                R.drawable.tau_transparent_pixel,
                context.getString(R.string.notification_ok_btn),
                resultPendingIntent);
    }

    private List<Long> removeOldNotificationTime(Context context) {
        List<Long> times = Utils.convertToLongList(Utils.getUniqueStringListFromPrefs(context, R.string.key_survey_notifications_times));
        ArrayList<Long> newTimes = new ArrayList<Long>();
        long timeInOneHour = Utils.getFutureMillisTimeByHours(1);

        // Go over the existing notification times and only add the ones that are in the far future (more than an hour from now) to the new list.
        // This way we remove old notifications from the list.
        for (long time: times) {
            if (time > timeInOneHour) {
                newTimes.add(time);
            }
        }

        Utils.setUniqueStringListToPrefs(context, R.string.key_survey_notifications_times, Utils.convertToStringList(newTimes));
        return newTimes;
    }
}
