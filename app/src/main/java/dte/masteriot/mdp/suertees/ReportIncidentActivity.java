package dte.masteriot.mdp.suertees;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.Locale;

import dte.masteriot.mdp.suertees.objects.Incident;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportIncidentActivity extends AppCompatActivity {

    // Views
    TextView date_view, location_view;
    EditText title_view, desc_view;
    Button bt_cancel, bt_submit;
    Spinner type_view;
    private RadioGroup urgencyGroup;
    private String urgency;

    // Firestore instance
    private FirebaseFirestore firestore;

    // Data variables
    String title, desc, type, date;
    private FusedLocationProviderClient locationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    Location location;

    //Data to populate the Incident Types
    String [] type_array={"Pothole", "Sidewalk", "Crosswalk", "Streetlight", "Traffic light", "Street sign", "Ramp", "Trash bin", "Other"};

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
                            location_view.setText("Lat: " + latitude + "\nLon: " + longitude);
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

            // Create an Incident object with userID
            Incident incident = new Incident(title, desc, type, date, locationText, urgency, userId);

            // Add data to Firestore
            firestore.collection("incidents").document(userId).collection("userIdIncidents")
                    .add(incident)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(ReportIncidentActivity.this, "Incident reported successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReportIncidentActivity.this, "Failed to report incident: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateFields(String title, String date, String location, String type, String urgency) {
        return !title.isEmpty() && !date.isEmpty() && !location.isEmpty() && !type.isEmpty() && !urgency.isEmpty();
    }

    public void cancel(View view) {
        Intent i = new Intent(ReportIncidentActivity.this, HomeActivity.class);
        startActivity(i);
    }
}