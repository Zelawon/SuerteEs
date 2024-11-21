package dte.masteriot.mdp.suertees.IncidentLists;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
                    Toast.makeText(ViewListsActivity.this, "No incidents found.", Toast.LENGTH_SHORT).show();
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

}