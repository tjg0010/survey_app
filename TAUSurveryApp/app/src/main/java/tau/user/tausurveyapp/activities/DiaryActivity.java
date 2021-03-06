package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

import tau.user.tausurveyapp.NetworkManager;
import tau.user.tausurveyapp.NotificationsManager;
import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.SurveyBuilder;
import tau.user.tausurveyapp.Utils;
import tau.user.tausurveyapp.contracts.Survey;
import tau.user.tausurveyapp.contracts.TauLocale;
import tau.user.tausurveyapp.types.NetworkCallback;
import tau.user.tausurveyapp.types.SurveySubmitResult;
import tau.user.tausurveyapp.types.SurveyType;

public class DiaryActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION = 1;
    private SurveyBuilder sb;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();

        // Set a loading title right away so we won't display the default title.
        setTitle(R.string.register_survey_loading_title);

        setContentView(R.layout.activity_diary);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.VISIBLE);
        final View btnRetry = findViewById(R.id.btnRetry);

        // Remove the notification from display (if any).
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        Utils.initializeErrorMsg(this,getString(R.string.diary_survey_error_title), getString(R.string.diary_survey_error_body));

        sb = new SurveyBuilder();

        NetworkManager.getInstance().getDiarySurvey(this, new NetworkCallback<Survey>() {
            @Override
            public void onResponse(Survey survey, boolean isSuccessful) {
                if (isSuccessful) {
                    sb.buildSurvey(DiaryActivity.this, survey, (LinearLayout)findViewById(R.id.contentView), TauLocale.IL);
                    progressBar.setVisibility(View.GONE);
                    Utils.toggleErrorMsg(DiaryActivity.this, false);
                    btnRetry.setVisibility(View.GONE);
                    DiaryActivity.this.toggleSubmitButton(true);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    DiaryActivity.this.toggleSubmitButton(false);
                    Utils.toggleErrorMsg(DiaryActivity.this, true);
                    btnRetry.setVisibility(View.VISIBLE);
                    DiaryActivity.this.setTitle(getString(R.string.diary_survey_default_title));
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                DiaryActivity.this.toggleSubmitButton(false);
                Utils.toggleErrorMsg(DiaryActivity.this, true);
                btnRetry.setVisibility(View.VISIBLE);
                DiaryActivity.this.setTitle(getString(R.string.diary_survey_default_title));
            }
        });
    }

    private void toggleSubmitButton(boolean show) {
        View submitButton = findViewById(R.id.btn_submit);
        if (submitButton != null) {
            submitButton.setVisibility(show ? View.VISIBLE : View.GONE);
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
                return sb.submitSurvey(DiaryActivity.this, SurveyType.DIARY);
            }

            @Override
            protected void onPostExecute(SurveySubmitResult surveySubmitResult) {
                // Update UI according to result. This runs in main UI thread.
                // Hide loading.
                progressBar.setVisibility(View.GONE);

                // If the survey submission was completed successfully.
                if (surveySubmitResult.isSuccess()) {
                    // Clear all notifications in the next day since this diary is answered, and reset snooze count to be ready for next time.
                    NotificationsManager.getInstance().resetCurrentNotifications(DiaryActivity.this);

                    // Go to the info screen.
                    Intent intent = new Intent(DiaryActivity.this, InfoActivity.class);
                    startActivity(intent);
                }
                // If an error occurred (network error, empty mandatory field was found, etc...)
                else {
                    button.setEnabled(true);
                    // Show an alert box with the error message.
                    Utils.showAlertBox(DiaryActivity.this, DiaryActivity.this.getString(R.string.survey_error_title),
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
                                View scrollView = DiaryActivity.this.findViewById(R.id.diary_scroll_view);
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }
}
