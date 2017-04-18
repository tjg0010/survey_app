package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import tau.user.tausurveyapp.NetworkCallback;
import tau.user.tausurveyapp.NetworkManager;
import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.SurveyBuilder;
import tau.user.tausurveyapp.TrackingRepeater;
import tau.user.tausurveyapp.Utils;
import tau.user.tausurveyapp.contracts.TauLocale;
import tau.user.tausurveyapp.contracts.Survey;

public class RegisterActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION = 1;
    private SurveyBuilder sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();

        // Set a loading title right away so we won't display the default title.
        setTitle(R.string.register_survey_loading_title);

        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.VISIBLE);

        Utils.initializeErrorMsg(this,getString(R.string.register_survey_error_title), getString(R.string.register_survey_error_body));

        sb = new SurveyBuilder();

        NetworkManager.getInstance().GetRegistrationSurvey(this, new NetworkCallback<Survey>() {
            @Override
            public void onResponse(Survey survey) {
                sb.BuildSurvey(RegisterActivity.this, survey, (LinearLayout)findViewById(R.id.contentView), TauLocale.IL);
                progressBar.setVisibility(View.GONE);
                Utils.toggleErrorMsg(RegisterActivity.this, false);
                RegisterActivity.this.toggleSubmitButton(true);
            }

            @Override
            public void onFailure(String error) {
                // TODO: show the user a failure message and log error.
                progressBar.setVisibility(View.GONE);
                RegisterActivity.this.toggleSubmitButton(false);
                Utils.toggleErrorMsg(RegisterActivity.this, true);
                RegisterActivity.this.setTitle(getString(R.string.register_survey_default_title));
            }
        });

        // Check if the app has location permissions. If not, request them.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permissions, request them.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
        // If we do have permissions.
        else {
            // Start the tracking.
            TrackingRepeater.getInstance().startRepeatedTracking(this, false);

            // TODO: add this logic to the info screen as well once it's all set up.
        }
    }

    private void toggleSubmitButton(boolean show) {
        View submitButton = findViewById(R.id.register_submit);
        if (submitButton != null) {
            submitButton.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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

    /**
     * Callback for ActivityCompat.requestPermissions. If the permissions are granted, start the tracking.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted!
                    TrackingRepeater.getInstance().startRepeatedTracking(this, false);
                }
                else {
                    // Permission denied! Do nothing...
                }
                return;
            }
        }
    }
}
