package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.TrackingRepeater;
import tau.user.tausurveyapp.Utils;

public class InfoActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();
        setContentView(R.layout.activity_info);

        // Set the title from the prefs (the title we got from the server).
        String infoTitle = Utils.getStringFromPrefs(this, R.string.key_info_screen_title);
        setTitle(infoTitle);

        // Get the body text from the prefs and set it.
        String infoText = Utils.getStringFromPrefs(this, R.string.key_info_screen_text);
        TextView infoTextView = (TextView)findViewById(R.id.txtInfo);
        // We parse the text as HTML to enable new lines with <br/>.
        infoTextView.setText(Utils.fromHtml(infoText));

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);

        // Check if the app has location permissions. If not, request them.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permissions, request them.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        initializeTracker();
    }

    private void initializeTracker() {
        // Start the tracking.
        TrackingRepeater.getInstance().startRepeatedTracking(InfoActivity.this, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button.
    }
}
