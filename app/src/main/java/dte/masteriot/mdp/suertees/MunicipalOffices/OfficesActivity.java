package dte.masteriot.mdp.suertees.MunicipalOffices;

import android.Manifest;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dte.masteriot.mdp.suertees.LightSensorManager;
import dte.masteriot.mdp.suertees.MunicipalOffices.adapter.MyAdapter;
import dte.masteriot.mdp.suertees.MunicipalOffices.model.DatasetOffices;
import dte.masteriot.mdp.suertees.R;

public class OfficesActivity extends AppCompatActivity implements OnDataLoadedListener {
    public static final String LOADWEBTAG = "LOAD_WEB_TAG";
    private static final String URL_OFFICES = "https://datos.madrid.es/egob/catalogo/200149-0-oficinas-linea-madrid.json";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String PREFS_NAME = "OfficesPrefs";
    private static final String PREFS_KEY_DATA = "WebContentData";
    private static final String TOPIC = "incidents";

    private MyAdapter recyclerViewAdapter;
    private FusedLocationProviderClient locationProviderClient;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private ExecutorService es;
    private Button btBack;
    private Button modeButton;
    private ProgressBar progressBar;
    private DatasetOffices dataset;
    private RecyclerView recyclerView;
    // Define the handler that will receive the messages from the background thread:
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // message received from background thread: load complete (or failure)
            String string_result = msg.getData().getString("text");
            if (string_result != null) {
                onDataLoaded(string_result);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OfficesActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Mqtt3AsyncClient mqttClient;
    private LightSensorManager lightSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offices);

        btBack = findViewById(R.id.buttonBack);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        modeButton = findViewById(R.id.button_mode);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedData = prefs.getString(PREFS_KEY_DATA, null);

        if (cachedData != null) {
            // Data exists in SharedPreferences, load it directly
            recyclerView.setVisibility(View.VISIBLE);
            onDataLoaded(cachedData);
        } else {
            // No cached data, proceed to download it
            progressBar.setVisibility(View.VISIBLE); // Show the progress bar
            es = Executors.newSingleThreadExecutor();
            LoadURLContents loadURLContents = new LoadURLContents(handler, CONTENT_TYPE_JSON, URL_OFFICES);
            es.execute(loadURLContents);
        }

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Request permissions and get the current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocationAndSort();
        }

        createMQTTClient();
        connectToBroker();
    }

    private void fetchLocationAndSort() {
        locationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            if (dataset != null) {
                                sortItemsByDistance();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OfficesActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    // Listener for the button:
    public void goBack(View view) {
        finish();
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
        Log.d(LOADWEBTAG, "Data loaded: " + data);

        // Hide the progress bar after loading the data
        progressBar.setVisibility(View.GONE);  // Hide the progress bar
        recyclerView.setVisibility(View.VISIBLE);

        // Save data to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_KEY_DATA, data);
        editor.apply();

        // Proceed with initializing dataset and RecyclerView
        dataset = new DatasetOffices(this, data);

        if (currentLatitude != 0 && currentLongitude != 0) {
            dataset.sortItemsByDistance(currentLatitude, currentLongitude);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new MyAdapter(dataset.getListOfItems());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter.notifyDataSetChanged();
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
                        Toast.makeText(OfficesActivity.this, "Failed to connect to broker", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(OfficesActivity.this, "Failed to subscribe to topic", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
