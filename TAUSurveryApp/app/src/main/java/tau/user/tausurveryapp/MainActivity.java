package tau.user.tausurveryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the shared preferences (key-value pairs). This file is shared between the different activities.
        SharedPreferences prefs = MainActivity.this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean(getString(R.string.key_is_registered), false);

        if (!isRegistered) {
            // TODO: go to registration activity
        }
        else {
            // TODO: go to main info activity
        }
    }
}
