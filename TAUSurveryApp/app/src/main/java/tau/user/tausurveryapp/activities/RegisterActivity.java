package tau.user.tausurveryapp.activities;

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

import tau.user.tausurveryapp.NetworkCallback;
import tau.user.tausurveryapp.NetworkManager;
import tau.user.tausurveryapp.R;
import tau.user.tausurveryapp.SurveyBuilder;
import tau.user.tausurveryapp.TrackingRepeater;
import tau.user.tausurveryapp.contracts.TauLocale;
import tau.user.tausurveryapp.contracts.Survey;

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

        sb = new SurveyBuilder();

        NetworkManager.getInstance().GetRegistrationSurvey(this, new NetworkCallback<Survey>() {
            @Override
            public void onResponse(Survey survey) {
                sb.BuildSurvey(RegisterActivity.this, survey, (LinearLayout)findViewById(R.id.contentView), TauLocale.IL);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                // TODO: show the user a failure message and log error.
                progressBar.setVisibility(View.GONE);
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

    // Function for validating numeric input fields
    public boolean numberEditTextValidation(int min, int max, int value){

        if ((value >= min) && (value <= max)){
            return true;
        }
        return false;
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


    /////////////// Code for hiding elements onclick /////////////////////
//
//    public void onClick(View v){
//
//        LinearLayout one = (LinearLayout) findViewById(R.id.LLSex);
//        one.setVisibility(View.GONE);
// }



}
