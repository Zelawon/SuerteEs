package dte.masteriot.mdp.suertees;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;

import dte.masteriot.mdp.suertees.accountmanagment.LoginActivity;
import dte.masteriot.mdp.suertees.viewlists.ViewListsActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
    }

    public void reportIssue(View v) {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);

        } else {
            // Permissions are already granted, check if location services are enabled
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "Please turn on location services to report an incident.", Toast.LENGTH_LONG).show();

                // Direct to the location settings
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            } else {
                Intent i = new Intent(HomeActivity.this, ReportIncidentActivity.class);
                startActivity(i);
            }
        }
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, call reportIssue() again
                    reportIssue(null);
                } else {
                    // Check if the user permanently denied the permission
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (!showRationale) {
                        // User chose "Don't ask again" or permanently denied permission
                        Toast.makeText(this, "Location permission is required. Please enable it in app settings.", Toast.LENGTH_LONG).show();

                        // Open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName())); // Set the URI to your package
                        startActivity(intent);
                    } else {
                        // Permission denied without "Don't ask again"
                        Toast.makeText(this, "Location permission is required to report an incident.", Toast.LENGTH_LONG).show();
                        // Ask for permission again
                        ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 1);
                    }
                }
            }
        }
    }

    public void seeAll(View v) {
        // TODO: Implement the logic if the DB is empty show empty message before Starting the Activity
        Intent intent = new Intent(HomeActivity.this, ViewListsActivity.class);
        intent.putExtra("type", "1111"); // Pass 1111 to say show all
        startActivity(intent);
    }

    public void seeMine(View v) {
        // TODO: Implement the logic if the DB is empty show empty message before Starting the Activity
        Intent intent = new Intent(HomeActivity.this, ViewListsActivity.class);
        intent.putExtra("type", "4444"); // Pass 4444 to say show mine
        startActivity(intent);
    }

    public void seeOffices(View v) {
        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(HomeActivity.this, OfficesActivity.class);
        // Once the intent is parametrized, start the second activity:
        startActivity(i);
    }

    // Method to handle toolbar icon click
    public void onMenuIconClick(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_logout, popupMenu.getMenu());

        // Set a listener for menu item clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MenuAction action = MenuAction.fromId(item.getItemId());
                if (action != null) {
                    switch (action) {
                        case LOGOUT:
                            logout();
                            return true;
                        // Add more cases for other actions as needed
                        default:
                            return false;
                    }
                }
                return false; // Return false if no action was found
            }
        });

        popupMenu.show(); // Show the popup menu
    }

    private void logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();
        // Redirect to a login activity
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        // Clear the activity stack and start the login activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }
}
