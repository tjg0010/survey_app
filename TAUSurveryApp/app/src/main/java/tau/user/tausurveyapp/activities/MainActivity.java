package tau.user.tausurveyapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.Utils;
import tau.user.tausurveyapp.types.PreferencesType;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);

        // Get the shared preferences (key-value pairs). This file is shared between the different activities.
        boolean isRegistered = Utils.getBooleanFromPrefs(MainActivity.this, R.string.key_is_registered);

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
