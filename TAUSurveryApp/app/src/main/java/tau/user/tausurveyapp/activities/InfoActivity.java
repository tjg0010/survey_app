package tau.user.tausurveyapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tau.user.tausurveyapp.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        initializeTracker();
    }

    private void initializeTracker() {
        // TODO: Put tracker initialization here.
    }
}