package tau.user.tausurveyapp.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.Utils;


public class IAgreeActivity extends AppCompatActivity {

    TextView Terms_and_Conditions_Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();
        setContentView(R.layout.activity_iagree);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tau_toolbar);
        setSupportActionBar(toolbar);

        // Set the title from the prefs (the title we got from the server).
        String agreeTitle = Utils.getStringFromPrefs(this, R.string.key_agree_screen_title);
        setTitle(agreeTitle);

        // Get the body text from the prefs and set it.
        String agreeText = Utils.getStringFromPrefs(this, R.string.key_agree_screen_text);
        TextView agreeTextView = (TextView)findViewById(R.id.txtTerms);
        // We parse the text as HTML to enable new lines with <br/>.
        agreeTextView.setText(Utils.fromHtml(agreeText));
        // Set scrolling on the text view.
        agreeTextView.setMovementMethod(new ScrollingMovementMethod());
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

    public void notAgree(View view) {
    }

    public void agree(View view) {
        CheckBox checkbox = (CheckBox)findViewById(R.id.agreeCheckBox);
        if(checkbox.isChecked()){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
        else{
            Snackbar.make(view, "אנא אשרו את טופס ההסכמה", Snackbar.LENGTH_LONG).show();
        }
    }
}
