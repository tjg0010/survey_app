package tau.user.tausurveryapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class IAgreeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iagree);
    }

    public void notAgree(View view) {
    }

    public void agree(View view) {


        // TODO: Migrate this to the right place (probably to the InfoActivity).
        TrackingRepeater.getInstance().startRepeatedTracking(this, false);
    }
}
