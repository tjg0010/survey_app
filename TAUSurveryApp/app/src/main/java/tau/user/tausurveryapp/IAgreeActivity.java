package tau.user.tausurveryapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.support.design.widget.Snackbar;


public class IAgreeActivity extends AppCompatActivity {

    TextView Terms_and_Conditions_Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iagree);

        Terms_and_Conditions_Text = (TextView)findViewById(R.id.Terms_and_Conditions_Text);
        Terms_and_Conditions_Text.setMovementMethod(new ScrollingMovementMethod());
    }


    public void notAgree(View view) {
    }

    public void agree(View view) {
        CheckBox checkbox = (CheckBox)findViewById(R.id.agreeCheckBox);
        if(checkbox.isChecked()){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }else{
            Snackbar.make(view, "אנא אשרו את טופס ההסכמה", Snackbar.LENGTH_LONG).show();




        }


//        // TODO: Migrate this to the right place (probably to the InfoActivity).
        TrackingRepeater.getInstance().startRepeatedTracking(this, false);
    }
}
