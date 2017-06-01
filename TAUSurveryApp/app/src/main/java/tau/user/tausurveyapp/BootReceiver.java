package tau.user.tausurveyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ran on 07/12/2016.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Restart tracking after reboot.
            TrackingRepeater.getInstance().startRepeatedTracking(context, true);
            // Re-register notifications after reboot.
            NotificationsManager.getInstance().activateNotifications(context);
            // Re-register bluetooth sampling after reboot.
            BluetoothSamplingManager.getInstance().activateNotifications(context);
        }
    }
}
