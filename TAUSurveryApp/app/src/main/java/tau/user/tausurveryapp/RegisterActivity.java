package tau.user.tausurveryapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        setTitle("טופס הרשמה");
        actionBar.setHomeButtonEnabled(true);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Check if the app has location permissions. If not, request them.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permissions, request them.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
        // If we do have permissions.
        else {
            // Start the tracking.
            TrackingRepeater.getInstance().startRepeatedTracking(this, false);

            // TODO: add this logic to the info screen as well once it's all set up.
        }
    }


    // Function for validating numeric input fields
    public boolean numberEditTextValidation(int min, int max, int value){

        if ((value >= min) && (value <= max)){
            return true;
        }
        return false;
    }

    /**
     * Callback for ActivityCompat.requestPermissions. If the permissions are granted, start the tracking.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted!
                    TrackingRepeater.getInstance().startRepeatedTracking(this, false);
                }
                else {
                    // Permission denied! Do nothing...
                }
                return;
            }
        }
    }


    /////////////// Code for hiding elements onclick /////////////////////
//
//    public void onClick(View v){
//
//        LinearLayout one = (LinearLayout) findViewById(R.id.LLSex);
//        one.setVisibility(View.GONE);
// }



}
