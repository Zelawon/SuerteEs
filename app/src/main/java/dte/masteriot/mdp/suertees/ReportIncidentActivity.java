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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.Locale;

public class ReportIncidentActivity extends AppCompatActivity implements View.OnClickListener {

    // References to the views in the layout that are going to be read:
    TextView date_view, location_view;
    EditText title_view, desc_view;
    Button bt_cancel;
    Button bt_submit;
    Spinner type_view;

    // Variables to hold the data read from the views:
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

        //Referring the Views
        title_view = (EditText) findViewById(R.id.editTextTitle);
        desc_view = (EditText) findViewById(R.id.editTextDesc);
        bt_cancel = (Button) findViewById(R.id.buttonCancel);
        bt_submit = (Button) findViewById(R.id.buttonSubmit);
        type_view = (Spinner) findViewById(R.id.spinnerType);
        date_view = (TextView) findViewById(R.id.textViewCurrentDate);
        location_view = (TextView) findViewById(R.id.textViewCurrentLocation);

        // Adapter to adapt the data from array to Spinner
        ArrayAdapter adapter= new ArrayAdapter(ReportIncidentActivity.this,
                android.R.layout.simple_spinner_item, type_array);
        type_view.setAdapter(adapter);

        //Date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        date = df.format(c);
        date_view.setText(date);

        //Location
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //if permission is not granted, request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted, get the current location
            getCurrentLocation();
        }
        location_view.setText("");

        //Setting Listener for Submit Button
        bt_submit.setOnClickListener(this);
    }


    private void getCurrentLocation() {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If not, request the location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Get the current location with high accuracy priority
        locationProviderClient.getCurrentLocation(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY, null)
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


    /*private void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Task<Location> locationTask = locationProviderClient.getLastLocation();
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Toast.makeText(ReportIncidentActivity.this, "Lat: " + latitude + ", Lon: " + longitude, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ReportIncidentActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }*/

    @Override
    public void onClick(View v) {

        //Getting the Values from Views (EditText, Switch & Spinner)
        title = title_view.getText().toString();
        desc = desc_view.getText().toString();
        type = type_view.getSelectedItem().toString();
        date = date_view.getText().toString();
        location = null;

        // Creating Intent For Navigating to Second Activity (Explicit Intent)
       /* Intent i = new Intent(ReportIncidentActivity.this,ListActivity.class);

        // Adding values to the intent data to pass them to Second Activity
        i.putExtra("name_key", title);
        i.putExtra("reg_key", desc);
        i.putExtra("country_key", type);

        i.putExtra("date_key", date);
        i.putExtra("location_key", location);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);*/
    }

    public void cancel(View view) {
        //navigate back to home screen
        Intent i = new Intent(ReportIncidentActivity.this, HomeActivity.class);

        startActivity(i);
    }

}