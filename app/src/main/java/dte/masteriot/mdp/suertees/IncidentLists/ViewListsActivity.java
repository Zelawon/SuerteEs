package dte.masteriot.mdp.suertees.IncidentLists;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.suertees.LightSensorManager;
import dte.masteriot.mdp.suertees.Objects.Incident;
import dte.masteriot.mdp.suertees.R;

public class ViewListsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncidentAdapter incidentAdapter;
    private List<Incident> incidentList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private CollectionReference userIncidentsRef; // Reference to user's incidents
    private ListenerRegistration listenerRegistration;
    private LightSensorManager lightSensorManager;
    private Button modeButton;
    private Mqtt3AsyncClient mqttClient;
    private static final String TOPIC = "incidents";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lists);

        modeButton = findViewById(R.id.button_mode);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        incidentAdapter = new IncidentAdapter(incidentList, this);
        recyclerView.setAdapter(incidentAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Fetch incidents when the activity is created
        fetchIncidents();

        // Create and connect the MQTT client
        createMQTTClient();
        connectToBroker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch incidents again when the activity resumes
        fetchIncidents();

        // Initialize the light sensor manager
        lightSensorManager = LightSensorManager.getInstance(this);
        lightSensorManager.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lightSensorManager.stopListening(); // Stop sensor updates when activity stops
    }

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


    private void fetchIncidents() {
        FirebaseAuth auth = FirebaseAuth.getInstance(); // Get the FirebaseAuth instance
        FirebaseUser currentUser = auth.getCurrentUser(); // Get the current authenticated user
        String key = getIntent().getStringExtra("type"); // 4444 mine and 1111 all

        if (key != null) {
            if (key.equals("1111")) {
                // Show all incidents
                userIncidentsRef = firestore.collection("incidents").document("allIncidents").collection("Incidents");
            } else if (key.equals("4444") && currentUser != null) {
                // Show incidents of the authenticated user
                String userId = currentUser.getUid();
                userIncidentsRef = firestore.collection("incidents").document(userId).collection("userIdIncidents");
            }

            // Fetch incidents from Firestore
            listenerRegistration = userIncidentsRef.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(ViewListsActivity.this, "Error loading incidents: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                incidentList.clear(); // Clear the existing list
                if (value != null && !value.isEmpty()) {
                    for (QueryDocumentSnapshot doc : value) {
                        Incident incident = doc.toObject(Incident.class);
                        incidentList.add(incident);
                    }
                    incidentAdapter.notifyDataSetChanged(); // Notify adapter of data change
                } else {
                    // Handle empty database
                    Toast.makeText(ViewListsActivity.this, "No incidents reported by this user.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            // Handle case where the key is invalid
            Toast.makeText(this, "Failed to load list. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove(); // Remove the listener to avoid memory leaks
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
                        Toast.makeText(ViewListsActivity.this, "Failed to connect to broker", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ViewListsActivity.this, "Failed to subscribe to topic", Toast.LENGTH_SHORT).show();
                    } else {

                    }
                });
    }

}