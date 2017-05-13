package tau.user.tausurveyapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by ran on 17/04/2017.
 */

public class Utils {
    public static Integer tryParse(String text) {
        try
        {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * A helper function that gets the a date as a long (unix time) according to the given year, month and day.
     * @return the given year month and day as UNIX time.
     */
    public static long getUnixDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static void initializeErrorMsg(Activity activity, String title, String body) {
        if (activity != null) {
            View errorTitle = activity.findViewById(R.id.tau_error_msg_title);
            View errorBody = activity.findViewById(R.id.tau_error_msg_body);

            if (errorTitle != null && errorBody != null) {
                ((TextView)errorTitle).setText(title);
                ((TextView)errorBody).setText(body);
            }
        }
    }

    public static void toggleErrorMsg(Activity activity, boolean show) {
        if (activity != null) {
            View errorMsg = activity.findViewById(R.id.tau_error_msg);

            if (errorMsg != null) {
                errorMsg.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    public static String getUserId(Context context) {
        // Get all of the device's accounts and go over them.
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            // Just take the first valid one we find.
            if (account != null && !TextUtils.isEmpty(account.name)) {
                return account.name;
            }
        }

        return null;
    }

    public static void showAlertBox(Activity activity, String title, String message, @StringRes int buttonTxt) {
        // From: https://developer.android.com/guide/topics/ui/dialogs.html

        // 1. Instantiate an AlertDialog.Builder with its constructor.
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // 2. Add the buttons.
        builder.setPositiveButton(buttonTxt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
            }
        });

        // 3. Chain together various setter methods to set the dialog characteristics.
        builder.setMessage(message).setTitle(title);

        // 4. Get the AlertDialog from create().
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
