package tau.user.tausurveryapp;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        setTitle("טופס הרשמה");
        actionBar.setHomeButtonEnabled(true);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    // Function for validating numeric input fields
    public boolean numberEditTextValidation(int min, int max, int value){

        if ((value >= min) && (value <= max)){
            return true;
        }
        return false;
    }


    /////////////// Code for hiding elements onclick /////////////////////
//
//    public void onClick(View v){
//
//        LinearLayout one = (LinearLayout) findViewById(R.id.LLSex);
//        one.setVisibility(View.GONE);
// }



}
