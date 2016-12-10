package tau.user.tausurveryapp;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LocationPermissionActivity extends AppCompatActivity {

    private final int LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_permission);

        // ************** IMPORTANT: This activity should only be started if location permissions are needed! **************
        // ************** That's because it immediately asks for location permissions from the user.

        // To be safe, check (probably again) if we have location permissions.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permissions, request them.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
        else
        {
            // This activity was called with no reason, do nothing and kill it.
            finish();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Check the result we got was for the permission we asked for.
        if (requestCode == LOCATION_PERMISSION) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to.

            }
            else {
                // TODO: Show explenation that the app can not run without location permissions and GPS.
            }
        }
    }
}
