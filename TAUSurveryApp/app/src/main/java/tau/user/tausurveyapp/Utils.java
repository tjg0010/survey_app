package tau.user.tausurveyapp;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

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
}
