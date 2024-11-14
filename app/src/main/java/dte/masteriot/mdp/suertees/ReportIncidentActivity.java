package dte.masteriot.mdp.suertees;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Locale;

import dte.masteriot.mdp.suertees.accountmanagment.LoginActivity;
import dte.masteriot.mdp.suertees.objects.Incident;

public class ReportIncidentActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    // Views
    TextView date_view, location_view;
    EditText title_view, desc_view;
    Button bt_cancel, bt_submit;
    Spinner type_view;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        // Initialize Views
        title_view = findViewById(R.id.editTextTitle);
        desc_view = findViewById(R.id.editTextDesc);
        bt_cancel = findViewById(R.id.buttonCancel);
        bt_submit = findViewById(R.id.buttonSubmit);
        type_view = findViewById(R.id.spinnerType);
        date_view = findViewById(R.id.textViewCurrentDate);
        location_view = findViewById(R.id.textViewCurrentLocation);
        urgencyGroup = findViewById(R.id.urgencyGroup);

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
    protected void onResume(){
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
                            String location_text = "Lat: " + String.format("%.4f",latitude) + "\nLon: " + String.format("%.4f",longitude) ;
                            location_view.setText(location_text);
                        } else {
                            Toast.makeText(ReportIncidentActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
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


    private boolean validateFields(String title, String date, String location, String type, String urgency) {
        return !title.isEmpty() && !date.isEmpty() && !location.isEmpty() && !type.isEmpty() && !urgency.isEmpty();
    }

    public void cancel(View view) {
        finish();  // Close the activity
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
        Intent intent = new Intent(ReportIncidentActivity.this, LoginActivity.class);
        // Clear the activity stack and start the login activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

}