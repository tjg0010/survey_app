package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

import tau.user.tausurveyapp.BluetoothSamplingManager;
import tau.user.tausurveyapp.NotificationsManager;
import tau.user.tausurveyapp.SurveyManager;
import tau.user.tausurveyapp.types.NetworkCallback;
import tau.user.tausurveyapp.NetworkManager;
import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.SurveyBuilder;
import tau.user.tausurveyapp.Utils;
import tau.user.tausurveyapp.contracts.TauLocale;
import tau.user.tausurveyapp.contracts.Survey;
import tau.user.tausurveyapp.types.SurveySubmitResult;
import tau.user.tausurveyapp.types.SurveyType;

public class RegisterActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION = 1;
    private SurveyBuilder sb;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();

        // Set a loading title right away so we won't display the default title.
        setTitle(R.string.register_survey_loading_title);

        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.VISIBLE);
        final View btnRetry = findViewById(R.id.btnRetry);

        Utils.initializeErrorMsg(this,getString(R.string.register_survey_error_title), getString(R.string.register_survey_error_body));

        sb = new SurveyBuilder();

        NetworkManager.getInstance().getRegistrationSurvey(this, new NetworkCallback<Survey>() {
            @Override
            public void onResponse(Survey survey, boolean isSuccessful) {
                if (isSuccessful) {
                    sb.buildSurvey(RegisterActivity.this, survey, (LinearLayout)findViewById(R.id.contentView), TauLocale.IL);
                    progressBar.setVisibility(View.GONE);
                    Utils.toggleErrorMsg(RegisterActivity.this, false);
                    btnRetry.setVisibility(View.GONE);
                    RegisterActivity.this.toggleSubmitButton(true);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    RegisterActivity.this.toggleSubmitButton(false);
                    Utils.toggleErrorMsg(RegisterActivity.this, true);
                    btnRetry.setVisibility(View.VISIBLE);
                    RegisterActivity.this.setTitle(getString(R.string.register_survey_default_title));
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                RegisterActivity.this.toggleSubmitButton(false);
                Utils.toggleErrorMsg(RegisterActivity.this, true);
                btnRetry.setVisibility(View.VISIBLE);
                RegisterActivity.this.setTitle(getString(R.string.register_survey_default_title));
            }
        });

        // Check if the app has location permissions. If not, request them.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permissions, request them.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
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
                View locationPermissionsContainer = findViewById(R.id.locationPermissionsContainer);
                View surveyContentView = findViewById(R.id.contentView);
                View submitButton = findViewById(R.id.btn_submit);

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted!
                    // Make sure error message is hidden and survey is visible.
                    locationPermissionsContainer.setVisibility(View.GONE);
                    surveyContentView.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                }
                else {
                    // Permission denied!
                    // Display a message saying the user has to give us location permissions to continue with the survey.
                    surveyContentView.setVisibility(View.GONE);
                    submitButton.setVisibility(View.GONE);
                    locationPermissionsContainer.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Fired when the user clicks the submit button.
     * @param button - the view that was clicked (the button).
     */
    public void submit(final View button) {
        button.setEnabled(false);
        // Show loading.
        progressBar.setVisibility(View.VISIBLE);

        // Call the submit function in the SurveyBuilder async.
        new AsyncTask<Activity, Void, SurveySubmitResult>() {
            @Override
            protected SurveySubmitResult doInBackground(Activity... params) {
                // Call submitSurvey in our SurveyBuilder.
                return sb.submitSurvey(RegisterActivity.this, SurveyType.REGISTER);
            }

            @Override
            protected void onPostExecute(SurveySubmitResult surveySubmitResult) {
                // Update UI according to result. This runs in main UI thread.
                // Hide loading.
                progressBar.setVisibility(View.GONE);

                // If registration was completed successfully.
                if (surveySubmitResult.isSuccess()) {
                    // Save a flag that indicates the user is registered.
                    Utils.setBooleanToPrefs(RegisterActivity.this, R.string.key_is_registered, true);

                    // Mark the survey as started (it will only run for 7 days).
                    SurveyManager.getInstance().surveyStarted(RegisterActivity.this);

                    // Set the notification dates, and activate the notifications.
                    // We send here null just in case the server haven't sent us anything. If the dates will be set, this will have no effect.
                    NotificationsManager.getInstance().setDates(RegisterActivity.this, null);
                    NotificationsManager.getInstance().activateNotifications(RegisterActivity.this);

                    // Activate bluetooth sampling.
                    BluetoothSamplingManager.getInstance().activateNotifications(RegisterActivity.this);

                    // Go to the info screen.
                    Intent intent = new Intent(RegisterActivity.this, InfoActivity.class);
                    startActivity(intent);
                }
                // If an error occurred (network error, empty mandatory field was found, etc...)
                else {
                    button.setEnabled(true);
                    // Show an alert box with the error message.
                    Utils.showAlertBox(RegisterActivity.this, RegisterActivity.this.getString(R.string.survey_error_title),
                                       surveySubmitResult.getErrorMessage(), R.string.survey_error_button);

                    // If an empty mandatory field was found.
                    ArrayList<Integer> emptyViewIds = surveySubmitResult.getEmptyViewIds();
                    if (emptyViewIds != null && !emptyViewIds.isEmpty()) {
                        // Go over the empty view ids.
                        boolean isFirstView = true;
                        for (Integer viewId: emptyViewIds) {
                            // Paint them a red border.
                            View view = findViewById(viewId);

                            // From: http://stackoverflow.com/questions/16884524/programmatically-add-border-to-linearlayout
                            GradientDrawable border = new GradientDrawable();
                            // Transparent background - the first two characters are the alpha value, FF being fully opaque, and 00 being fully transparent.
                            border.setColor(0x00FFFFFF);
                            // Red border with full opacity.
                            border.setStroke(2, 0xFFE81717);
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                view.setBackgroundDrawable(border);
                            }
                            else {
                                view.setBackground(border);
                            }

                            // Scroll to the first view in the list.
                            if (isFirstView) {
                                view.getParent().requestChildFocus(view,view);
                                View scrollView = RegisterActivity.this.findViewById(R.id.register_scroll_view);
                                //noinspection ResourceType
                                scrollView.scrollBy(0, -20);
                            }
                            isFirstView = false;
                        }
                    }
                }
            }
        }.execute();
    }

    /**
     * Fired when the user clicks the retry button.
     * @param button - the view that was clicked (the button).
     */
    public void reload(final View button) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Fired when the user clicks the "add permissions" button.
     * @param button - the view that was clicked (the button).
     */
    public void showLocationPermissionsDialog(final View button) {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
    }

    private void toggleSubmitButton(boolean show) {
        View submitButton = findViewById(R.id.btn_submit);
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
}
