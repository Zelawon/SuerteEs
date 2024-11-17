package dte.masteriot.mdp.suertees;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import dte.masteriot.mdp.suertees.AccountManagment.LoginActivity;
import dte.masteriot.mdp.suertees.IncidentLists.ViewListsActivity;
import dte.masteriot.mdp.suertees.MunicipalOffices.OfficesActivity;

public class HomeActivity extends AppCompatActivity {

    private static final String TOPIC = "incidents";
    private LightSensorManager lightSensorManager;
    private Button modeButton;
    private Mqtt3AsyncClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        modeButton = findViewById(R.id.button_mode);

        Button logoutButton = findViewById(R.id.log_out_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the logout method when the button is clicked
                logout();
            }
        });

        // Register the back press callback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show the confirmation dialog here
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("Are you sure you want to log out?")
                        .setCancelable(false)  // Prevent closing dialog by tapping outside
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Sign out from Firebase
                                FirebaseAuth.getInstance().signOut();
                                // Redirect to LoginActivity
                                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                // Clear the activity stack and start the login activity
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                finish(); // Finish the current activity
                            }
                        })
                        .setNegativeButton("No", null) // Dismiss the dialog on "No"
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Create and connect the MQTT client
        createMQTTClient();
        connectToBroker();
    }

    private void createMQTTClient() {
        mqttClient = MqttClient.builder()
                .useMqttVersion3()
                .identifier("home-activity-subscriber")
                .serverHost("192.168.56.1")  // MQTT Broker IP address
                .serverPort(1883)  // MQTT Broker port
                .buildAsync();
    }

    private void connectToBroker() {
        mqttClient.connectWith()
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        Toast.makeText(HomeActivity.this, "Failed to connect to broker", Toast.LENGTH_SHORT).show();
                    } else {
                        subscribeToTopic();
                    }
                });
    }

    private void subscribeToTopic() {
        mqttClient.subscribeWith()
                .topicFilter(TOPIC)  // The topic to subscribe to
                .callback(publish -> {
                    String message = new String(publish.getPayloadAsBytes());
                    runOnUiThread(() -> {
                        int startIndex = message.indexOf("ID:") + 3;  // Start after "ID:"
                        int endIndex = message.indexOf("Incident:");  // End before "Incident:"
                        String id = message.substring(startIndex, endIndex).trim();
                        int startIndex2 = message.indexOf("Incident:");
                        String incidentMessage = message.substring(startIndex2).trim();
                        // Check if the authenticated user ID matches the ID in the message
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if (currentUserId.equals(id)) {
                            // Show the received message as a Toast if the IDs match
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), incidentMessage, Snackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            // Change background color
//                            snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbarBackground));
                            // Adjust the text size and padding
                            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextSize(18); // Set larger text size
                            textView.setPadding(32, 32, 32, 32); // Increase padding
                            textView.setTextColor(Color.WHITE); // Set text color

                            snackbar.show();
                        }
                    });
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Toast.makeText(HomeActivity.this, "Failed to subscribe to topic", Toast.LENGTH_SHORT).show();
                    } else {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialize the light sensor manager
        lightSensorManager = LightSensorManager.getInstance(this);
        lightSensorManager.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lightSensorManager.stopListening(); // Stop sensor updates when activity stops
    }

    // Report an issue with location permission check
    public void reportIssue(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if they are not granted
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

    // Handle permission request result
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
                        Toast.makeText(this, "Location permission is required. Enable it in app settings.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
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

    public void changeTheme(View v) {
        // Check the current mode and toggle it
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();

        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            Log.d("button", "Switching to light mode");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            Log.d("button", "Switching to dark mode");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        getDelegate().applyDayNight();
    }

    public void logout() {
        // Create a confirmation dialog
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)  // Prevent closing dialog by tapping outside
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Sign out from Firebase
                        FirebaseAuth.getInstance().signOut();
                        // Redirect to a login activity
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        // Clear the activity stack and start the login activity
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null) // Dismiss the dialog on "No"
                .show();
    }


}
