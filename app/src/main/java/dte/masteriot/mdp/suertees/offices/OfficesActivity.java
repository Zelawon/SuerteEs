package dte.masteriot.mdp.suertees.offices;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dte.masteriot.mdp.suertees.HomeActivity;
import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.ReportIncidentActivity;
import dte.masteriot.mdp.suertees.offices.model.DatasetOffices;
import dte.masteriot.mdp.suertees.offices.adapter.MyAdapter;
import dte.masteriot.mdp.suertees.offices.model.Item;


public class OfficesActivity extends AppCompatActivity implements OnDataLoadedListener {
    public static final String LOADWEBTAG = "LOAD_WEB_TAG"; // to easily filter logs
    private static final String URL_OFFICES = "https://datos.madrid.es/egob/catalogo/200149-0-oficinas-linea-madrid.json";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private FusedLocationProviderClient locationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private double currentLatitude;
    private double currentLongitude;

    private ExecutorService es;
    private Button btBack;
    private TextView text;

    private DatasetOffices dataset;
    private RecyclerView recyclerView;
    MyAdapter recyclerViewAdapter;

    private static final String PREFS_NAME = "OfficesPrefs";
    private static final String PREFS_KEY_DATA = "WebContentData";


    // Define the handler that will receive the messages from the background thread:
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // message received from background thread: load complete (or failure)
            super.handleMessage(msg);
            String string_result = msg.getData().getString("text");
            if (string_result != null) {
                // Call the onDataLoaded callback with the loaded data
                onDataLoaded(string_result);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offices);

        btBack = findViewById(R.id.buttonBack);
        text = findViewById(R.id.HTTPTextView);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedData = prefs.getString(PREFS_KEY_DATA, null);

        if (cachedData != null) {
            // Data exists in SharedPreferences, load it directly
            onDataLoaded(cachedData);
        } else {
            // No cached data, proceed to download it
            es = Executors.newSingleThreadExecutor();
            Toast.makeText(this, "Loading Data", Toast.LENGTH_SHORT).show();
            LoadURLContents loadURLContents = new LoadURLContents(handler, CONTENT_TYPE_JSON, URL_OFFICES);
            es.execute(loadURLContents);

        }

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Request permissions and get the current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();

                                // Call sortItemsByDistance once location is available
                                sortItemsByDistance();
                            }
                        }
                    });
        }
    }

    // Listener for the button:
    public void goBack(View view) {
        // Creating Intent For Navigating to HomeActivity
        Intent i = new Intent(OfficesActivity.this, HomeActivity.class);
        startActivity(i);
    }

    private void sortItemsByDistance() {
        if (dataset != null) {
            // Sort the offices by distance to the current location
            dataset.sortItemsByDistance(currentLatitude, currentLongitude);

            // Notify the adapter that the data has changed and the RecyclerView should be refreshed
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataLoaded(String data) {
        text.setText(null);
        Log.d(LOADWEBTAG, "Data loaded: " + data);

        // Save data to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_KEY_DATA, data);
        editor.apply();

        // Proceed with initializing dataset and RecyclerView
        dataset = new DatasetOffices(this, data);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new MyAdapter(dataset.getListOfItems());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


}