package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.Utils;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();
        setContentView(R.layout.activity_welcome);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);

        // Get the title from the prefs and set it.
        String welcomeTitle = Utils.getStringFromPrefs(this, R.string.key_welcome_screen_title);
        setTitle(welcomeTitle);

        // Get the body text from the prefs and set it.
        String welcomeText = Utils.getStringFromPrefs(this, R.string.key_welcome_screen_text);
        TextView welcomeTextView = (TextView)findViewById(R.id.txtWelcome);
        // We parse the text as HTML to enable new lines and some decoration.
        welcomeTextView.setText(Utils.fromHtml(welcomeText));
    }

    /**
     * Fired when the user clicks the continue button.
     * @param button - the view that was clicked (the button).
     */
    public void continue_clicked(View button) {
        // Send the user to the IAgree activity.
        Intent intent = new Intent(this, IAgreeActivity.class);
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
