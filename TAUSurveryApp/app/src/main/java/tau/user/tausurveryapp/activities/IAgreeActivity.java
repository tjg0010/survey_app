package tau.user.tausurveryapp.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import tau.user.tausurveryapp.R;


public class IAgreeActivity extends AppCompatActivity {

    TextView Terms_and_Conditions_Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();
        setContentView(R.layout.activity_iagree);

        Terms_and_Conditions_Text = (TextView)findViewById(R.id.Terms_and_Conditions_Text);
        Terms_and_Conditions_Text.setMovementMethod(new ScrollingMovementMethod());

        setTitle("טופס הסכמה");
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
