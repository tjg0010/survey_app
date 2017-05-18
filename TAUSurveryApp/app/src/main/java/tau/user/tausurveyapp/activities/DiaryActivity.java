package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.app.Activity;
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

        Utils.initializeErrorMsg(this,getString(R.string.diary_survey_error_title), getString(R.string.diary_survey_error_body));

        sb = new SurveyBuilder();

        NetworkManager.getInstance().getDiarySurvey(this, new NetworkCallback<Survey>() {
            @Override
            public void onResponse(Survey survey) {
                sb.buildSurvey(DiaryActivity.this, survey, (LinearLayout)findViewById(R.id.contentView), TauLocale.IL);
                progressBar.setVisibility(View.GONE);
                Utils.toggleErrorMsg(DiaryActivity.this, false);
                DiaryActivity.this.toggleSubmitButton(true);
            }

            @Override
            public void onFailure(String error) {
                // TODO: show the user a failure message and log error.
                progressBar.setVisibility(View.GONE);
                DiaryActivity.this.toggleSubmitButton(false);
                Utils.toggleErrorMsg(DiaryActivity.this, true);
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
     * @param view - the view that was clicked (the button).
     */
    public void submit(View view) {
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

                // If registration was completed successfully.
                if (surveySubmitResult.isSuccess()) {
                    // Save a flag that indicates the user is registered.
                    Utils.setBooleanToPrefs(DiaryActivity.this, R.string.key_is_diary1_answered, true);

                    // TODO: if diary 1 is marked as answered, mark diary 2 as answered.

                    // Go to the info screen.
                    // TODO: The info screen should change its text according to the flags in the storage.
                    Intent intent = new Intent(DiaryActivity.this, InfoActivity.class);
                    startActivity(intent);
                }
                // If an error occurred (network error, empty mandatory field was found, etc...)
                else {
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }
}
