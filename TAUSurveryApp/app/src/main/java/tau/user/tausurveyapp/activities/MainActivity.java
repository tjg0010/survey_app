package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import tau.user.tausurveyapp.NetworkManager;
import tau.user.tausurveyapp.NotificationsManager;
import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.Utils;
import tau.user.tausurveyapp.contracts.SurveyInfo;
import tau.user.tausurveyapp.contracts.TauLocale;
import tau.user.tausurveyapp.types.NetworkCallback;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();
        setContentView(R.layout.activity_main);

        setTitle(R.string.main_screen_title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBarMain);
        final View btnRetry = findViewById(R.id.btnRetry);

        Utils.initializeErrorMsg(this,getString(R.string.main_screen_error_title), getString(R.string.main_screen_error_body));

        // Get the shared preferences (key-value pairs). This file is shared between the different activities.
        boolean isRegistered = Utils.getBooleanFromPrefs(MainActivity.this, R.string.key_is_registered);

        // If the user hasn't yet registered.
        if (!isRegistered) {
            // Get the surveys info.
            NetworkManager.getInstance().getSurveyInfo(this, new NetworkCallback<SurveyInfo>() {
                @Override
                public void onResponse(SurveyInfo surveyInfo, boolean isSuccessful) {
                    progressBar.setVisibility(View.GONE);
                    Utils.toggleErrorMsg(MainActivity.this, false);
                    btnRetry.setVisibility(View.GONE);

                    if (isSuccessful) {
                        // Save the diary surveys dates we got.
                        NotificationsManager.getInstance().setDates(MainActivity.this, surveyInfo.diaryDates);

                        // Save the welcome screen texts.
                        Utils.setStringToPrefs(MainActivity.this, R.string.key_welcome_screen_title, surveyInfo.getString(TauLocale.IL, surveyInfo.welcomeScreen.title));
                        Utils.setStringToPrefs(MainActivity.this, R.string.key_welcome_screen_text, surveyInfo.getString(TauLocale.IL, surveyInfo.welcomeScreen.text));

                        // Save the agree screen texts.
                        Utils.setStringToPrefs(MainActivity.this, R.string.key_agree_screen_title, surveyInfo.getString(TauLocale.IL, surveyInfo.agreeScreen.title));
                        Utils.setStringToPrefs(MainActivity.this, R.string.key_agree_screen_text, surveyInfo.getString(TauLocale.IL, surveyInfo.agreeScreen.text));

                        // Save the info screen texts.
                        Utils.setStringToPrefs(MainActivity.this, R.string.key_info_screen_title, surveyInfo.getString(TauLocale.IL, surveyInfo.infoScreen.title));
                        Utils.setStringToPrefs(MainActivity.this, R.string.key_info_screen_text, surveyInfo.getString(TauLocale.IL, surveyInfo.infoScreen.text));

                        if (surveyInfo.isUserRegistered) {
                            // If this user is already registered, mark him as such and send him to the info page (that makes sure the tracker is on).
                            Utils.setBooleanToPrefs(MainActivity.this, R.string.key_is_registered, true);
                            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                            startActivity(intent);
                        }
                        else {
                            // The user really not yet registered. Send him to the welcome activity.
                            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                        }
                    }
                    else {
                        Utils.toggleErrorMsg(MainActivity.this, true);
                        btnRetry.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    Utils.toggleErrorMsg(MainActivity.this, true);
                    btnRetry.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            // Go to info activity.
             Intent intent = new Intent(this, InfoActivity.class);
             startActivity(intent);
        }
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

    @Override
    public void onBackPressed() {
        // Disable back button.
    }
}
