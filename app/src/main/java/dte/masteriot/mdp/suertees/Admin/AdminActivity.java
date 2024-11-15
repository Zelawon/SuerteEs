package dte.masteriot.mdp.suertees.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.suertees.MenuAction;
import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.accountmanagment.LoginActivity;
import dte.masteriot.mdp.suertees.objects.Incident;
import dte.masteriot.mdp.suertees.viewlists.IncidentAdapter;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IncidentAdapter incidentAdapter;
    private List<Incident> incidentList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private CollectionReference userIncidentsRef; // Reference to user's incidents
    private ListenerRegistration listenerRegistration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        incidentAdapter = new IncidentAdapter(incidentList, this);
        recyclerView.setAdapter(incidentAdapter);

        firestore = FirebaseFirestore.getInstance();

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        setSupportActionBar(bottomAppBar);

        // Fetch incidents when the activity is created
        fetchIncidents();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_navigation_menu_admin, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch incidents again when the activity resumes
        fetchIncidents();
    }

    private void fetchIncidents() {
        FirebaseAuth auth = FirebaseAuth.getInstance(); // Get the FirebaseAuth instance
        userIncidentsRef = firestore.collection("incidents").document("allIncidents").collection("Incidents");

        // Fetch incidents from Firestore
        listenerRegistration = userIncidentsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(AdminActivity.this, "Error loading incidents: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            incidentList.clear(); // Clear the existing list
            if (value != null && !value.isEmpty()) {
                for (QueryDocumentSnapshot doc : value) {
                    Incident incident = doc.toObject(Incident.class);
                    incidentList.add(incident);
                }
                incidentAdapter.notifyDataSetChanged(); // Notify adapter of data change
                findViewById(R.id.noIncidentsText).setVisibility(View.GONE); // Hide "No incidents found" message if data is present
            } else {
                // Show "No incidents found" message if Firestore is empty
                findViewById(R.id.noIncidentsText).setVisibility(View.VISIBLE); // Show the "No incidents found" TextView
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove(); // Remove the listener to avoid memory leaks
        }
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
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        // Clear the activity stack and start the login activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }
}