package dte.masteriot.mdp.suertees.viewlists;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.objects.Incident;

public class ViewListsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncidentAdapter incidentAdapter;
    private List<Incident> incidentList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private CollectionReference userIncidentsRef; // Reference to user's incidents
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_lists);

        String key = getIntent().getStringExtra("type"); // 4444 mine and 1111 all

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        incidentAdapter = new IncidentAdapter(incidentList, this);
        recyclerView.setAdapter(incidentAdapter);

        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance(); // Get the FirebaseAuth instance
        FirebaseUser currentUser = auth.getCurrentUser(); // Get the current authenticated user

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
