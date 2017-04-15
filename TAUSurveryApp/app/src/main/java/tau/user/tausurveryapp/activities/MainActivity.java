package tau.user.tausurveryapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tau.user.tausurveryapp.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the shared preferences (key-value pairs). This file is shared between the different activities.
        SharedPreferences prefs = MainActivity.this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean(getString(R.string.key_is_registered), false);

        // TODO: Consider adding a delay, so the user actually sees this activity...

        if (!isRegistered) {
            // Go to registration activity.
            Intent intent = new Intent(this, IAgreeActivity.class);
            startActivity(intent);
        }
        else {
            // Go to info activity.
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
        }
    }
}
