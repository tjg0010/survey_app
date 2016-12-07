package tau.user.tausurveryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        initializeTracker();
    }

    private void initializeTracker() {
        // Make sure alarm (tracking) is not already set by checking in our shared preferences.
        SharedPreferences prefs = InfoActivity.this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isTracking = prefs.getBoolean(getString(R.string.key_is_tracking), false);

        // If the alarm is not started, we should start it.
        if (!isTracking) {

        }
    }
}
