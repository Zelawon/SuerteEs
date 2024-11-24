package dte.masteriot.mdp.suertees;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.Date;
import java.util.Locale;

import dte.masteriot.mdp.suertees.Objects.Incident;

public class ReportIncidentActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TOPIC = "incidents";
    // Views
    TextView date_view, location_view;
    EditText title_view, desc_view;
    Button bt_cancel, bt_submit;
    Spinner type_view;
    Button modeButton;
    // Data variables
    String title, desc, type, date;
    Location location;
    //Data to populate the Incident Types
    String[] type_array = {"Pothole", "Sidewalk", "Crosswalk", "Streetlight", "Traffic light", "Street sign", "Ramp", "Trash bin", "Other"};
    private RadioGroup urgencyGroup;
    private String urgency;
    // Firestore instance
    private FirebaseFirestore firestore;
    private FusedLocationProviderClient locationProviderClient;
    private LightSensorManager lightSensorManager;
    private Mqtt3AsyncClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);
        hideKeyboard();

        // Initialize Views
        title_view = findViewById(R.id.editTextTitle);
        desc_view = findViewById(R.id.editTextDesc);
        bt_cancel = findViewById(R.id.buttonCancel);
        bt_submit = findViewById(R.id.buttonSubmit);
        type_view = findViewById(R.id.spinnerType);
        date_view = findViewById(R.id.textViewCurrentDate);
        location_view = findViewById(R.id.textViewCurrentLocation);
        urgencyGroup = findViewById(R.id.urgencyGroup);
        modeButton = findViewById(R.id.button_mode);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Adapter for Spinner
        ArrayAdapter adapter = new ArrayAdapter(ReportIncidentActivity.this,
                android.R.layout.simple_spinner_item, type_array);
        type_view.setAdapter(adapter);

        // Set the current date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        date = df.format(c);
        date_view.setText(date);

        // Initialize location services
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }

        // Add a callback for the back button [control the behavior of the App]
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Finish the activity when back button is pressed
                finish();  // Close the activity
            }
        });

        // Create and connect the MQTT client
        createMQTTClient();
        connectToBroker();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        title = sharedPref.getString("title", title);
        desc = sharedPref.getString("desc", desc);
        type = sharedPref.getString("type", type);
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

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("title", title);
        editor.putString("desc", desc);
        editor.putString("type", type);
        editor.commit();

    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        locationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String location_text = "Lat: " + String.format("%.4f", latitude) + "\nLon: " + String.format("%.4f", longitude);
                            location_view.setText(location_text);
                        } else {
                            Toast.makeText(ReportIncidentActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportIncidentActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void submit(View v) {
        // Get values from Views
        title = title_view.getText().toString().trim();
        desc = desc_view.getText().toString().trim();
        type = type_view.getSelectedItem().toString();
        date = date_view.getText().toString().trim();
        String locationText = location_view.getText().toString().trim();

        int selectedId = urgencyGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            urgency = selectedRadioButton.getText().toString();
        } else {
            urgency = "";
        }

        if (validateFields(title, date, locationText, type, urgency)) {
            // Get the current user ID
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Generate a unique ID for the incident
            String incidentId = firestore.collection("incidents").document().getId();

            // Create an Incident object with userID and generated ID
            Incident incident = new Incident(incidentId, title, desc, type, date, locationText, urgency, userId);
            // Show a progress dialog while adding the incident
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Reporting incident...");
            progressDialog.setCancelable(false); // Prevent dismissal by back button
            progressDialog.show();

            // Add data to Firestore
            firestore.collection("incidents").document(userId).collection("userIdIncidents")
                    .document(incidentId)
                    .set(incident)
                    .addOnSuccessListener(unused -> {
                        // Add the incident to a collection for all incidents for faster queries later
                        firestore.collection("incidents").document("allIncidents").collection("Incidents")
                                .document(incidentId) // Use the same incident ID here
                                .set(incident)
                                .addOnSuccessListener(unused1 -> {
                                    // Successfully added to all incidents collection
                                    progressDialog.dismiss(); // Dismiss the progress dialog
                                    Toast.makeText(ReportIncidentActivity.this, "Incident reported successfully", Toast.LENGTH_SHORT).show();
                                    finish();  // Close the activity after adding the incident to both documents
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss(); // Dismiss the progress dialog
                                    Toast.makeText(ReportIncidentActivity.this, "Failed to add to all incidents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss(); // Dismiss the progress dialog
                        Toast.makeText(ReportIncidentActivity.this, "Failed to report incident: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
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

    private boolean validateFields(String title, String date, String location, String type, String urgency) {
        return !title.isEmpty() && !date.isEmpty() && !location.isEmpty() && !type.isEmpty() && !urgency.isEmpty();
    }

    public void cancel(View view) {
        finish();  // Close the activity
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
                        Toast.makeText(ReportIncidentActivity.this, "Failed to connect to broker", Toast.LENGTH_SHORT).show();
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
                            snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background));
                            // Adjust the text size and padding
                            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTextSize(18); // Set larger text size
                            textView.setPadding(32, 32, 32, 32); // Increase padding
//                            textView.setTextColor(ContextCompat.getColor(this, R.color.card_text)); // Set text color
                            snackbar.show();
                        }
                    });
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Toast.makeText(ReportIncidentActivity.this, "Failed to subscribe to topic", Toast.LENGTH_SHORT).show();
                    } else {

                    }
                });
    }
}