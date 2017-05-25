package tau.user.tausurveyapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tau.user.tausurveyapp.types.PreferencesType;

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

    public static List<String> convertToStringList(List<Long> list) {
        ArrayList<String> result = new ArrayList<>();
        for (long item: list) {
            result.add(Long.toString(item));
        }
        return result;
    }

    public static List<Long> convertToLongList(List<String> list) {
        ArrayList<Long> result = new ArrayList<>();
        for (String item: list) {
            result.add(Long.parseLong(item));
        }
        return result;
    }

    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
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

    public static long getCurrentTimeInMillis() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public static long getFutureMillisTimeByDays(int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTimeInMillis();
    }

    public static long getFutureMillisTimeByHours(int hoursToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, hoursToAdd);
        return cal.getTimeInMillis();
    }

    public static long getFutureMillisTimeByMinutes(int minutesToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutesToAdd);
        return cal.getTimeInMillis();
    }

    public static long getFutureMillisTimeByHourInDay(int hourInDay) {
        Calendar cal = Calendar.getInstance();

        while (cal.get(Calendar.HOUR_OF_DAY) != hourInDay) {
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

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

    public static String hashStringMD5(String str) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash.
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String.
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // If we failed, return a normal string hash.
        return String.valueOf(str.hashCode());
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

    public static String getStringFromPrefs(Context context, @StringRes int resId) {
        return (String)getFromPrefs(PreferencesType.STRING, context, resId);
    }

    public static void setStringToPrefs(Context context, @StringRes int resId, String value) {
        setToPrefs(PreferencesType.STRING, context, resId, value);
    }

    public static int getIntFromPrefs(Context context, @StringRes int resId) {
        return (int)getFromPrefs(PreferencesType.INT, context, resId);
    }

    public static void setIntToPrefs(Context context, @StringRes int resId, int value) {
        setToPrefs(PreferencesType.INT, context, resId, value);
    }

    public static long getLongFromPrefs(Context context, @StringRes int resId) {
        return (long)getFromPrefs(PreferencesType.LONG, context, resId);
    }

    public static void setLongToPrefs(Context context, @StringRes int resId, long value) {
        setToPrefs(PreferencesType.LONG, context, resId, value);
    }

    public static boolean getBooleanFromPrefs(Context context, @StringRes int resId) {
        return (boolean)getFromPrefs(PreferencesType.BOOLEAN, context, resId);
    }

    public static void setBooleanToPrefs(Context context, @StringRes int resId, boolean value) {
        setToPrefs(PreferencesType.BOOLEAN, context, resId, value);
    }

    public static List<String> getUniqueStringListFromPrefs(Context context, @StringRes int resId) {
        return getUniqueStringListFromPrefs(context, context.getString(resId));
    }

    public static void setUniqueStringListToPrefs(Context context, @StringRes int resId, List<String> list) {
        setUniqueStringListToPrefs(context, context.getString(resId), list);
    }

    public static List<String> getUniqueStringListFromPrefs(Context context, String id) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Set<String> stringSet = prefs.getStringSet(id, new HashSet<String>());
        ArrayList<String> results = new ArrayList<String>();
        results.addAll(stringSet);
        return results;
    }

    public static void setUniqueStringListToPrefs(Context context, String id, List<String> list) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(id, list == null ? null : new HashSet<String>(list));
        editor.apply();
    }

    public static <T> List<T> geObjectListFromPrefs(Class cls, Context context, @StringRes int resId) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Set<String> stringSet = prefs.getStringSet(context.getString(resId), new HashSet<String>());
        // Convert to the requested object list using gson.
        ArrayList<T> resultList = new ArrayList<T>();

        for (String item: stringSet) {
            Gson gson = new Gson();
            resultList.add((T)gson.fromJson(item, cls));
        }

        return resultList;
    }

    public static <T> void setObjectListToPrefs(Context context, @StringRes int resId, List<T> list) {
        // Prepare the prefs editor.
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Handle null (used to clear the key)
        if (list == null) {
            editor.putStringSet(context.getString(resId), null);
            editor.apply();
            return;
        }

        // Prepare the set (create a string set from the given List<T> with gson).
        HashSet<String> set = new HashSet<String>();
        for (T item: list) {
            Gson gson = new Gson();
            set.add(gson.toJson(item));
        }

        // Save the set to the prefs.
        editor.putStringSet(context.getString(resId), set);
        editor.apply();
    }

    public static boolean prefsContain(Context context, @StringRes int resId) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return prefs.contains(context.getString(resId));
    }

    /**
     * Gets the object with the given key (resId) and given PreferencesType from the shared preferences.
     * @param prefsType - the preferences type to get.
     * @param context - the current context. Used to get resource strings.
     * @param resId - the resId of the desired key from preferences.
     * @return the found result or an empty value if not found. If an unknown type is requested, null will be returned.
     */
    private static Object getFromPrefs(PreferencesType prefsType, Context context, @StringRes int resId) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Object result = null;

        switch (prefsType) {
            case STRING:
                result = prefs.getString(context.getString(resId), "");
                break;
            case INT:
                result = prefs.getInt(context.getString(resId), 0);
                break;
            case LONG:
                result = prefs.getLong(context.getString(resId), 0);
                break;
            case BOOLEAN:
                result = prefs.getBoolean(context.getString(resId), false);
                break;
        }

        return result;
    }

    /**
     * Sets a new object value to the given key (resId) in the shared preferences.
     * @param prefsType - the object's type to save. The object will be casted to the requested type.
     * @param context - the current context. Used to get resource strings.
     * @param resId - the resId of the desired key from preferences.
     * @param value - the new value to write in the given key.
     */
    private static void setToPrefs(PreferencesType prefsType, Context context, @StringRes int resId, Object value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        switch (prefsType) {
            case STRING:
                editor.putString(context.getString(resId), (String)value);
                editor.apply();
                break;
            case INT:
                editor.putInt(context.getString(resId), (Integer)value);
                editor.apply();
                break;
            case LONG:
                editor.putLong(context.getString(resId), (Long)value);
                editor.apply();
                break;
            case BOOLEAN:
                editor.putBoolean(context.getString(resId), (Boolean)value);
                editor.apply();
                break;
        }
    }
}
