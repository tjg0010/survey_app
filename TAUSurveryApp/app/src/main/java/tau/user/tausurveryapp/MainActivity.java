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

        // Get the preferences (key-value pairs) of this activity.
        SharedPreferences prefs = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean(getString(R.string.key_is_registered), false);

        if (!isRegistered) {
            // TODO: go to registration activity
        }
        else {
            // TODO: go to main info activity
        }
    }
}
