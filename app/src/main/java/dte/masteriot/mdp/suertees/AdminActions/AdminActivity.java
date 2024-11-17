package dte.masteriot.mdp.suertees.AdminActions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.suertees.AccountManagment.LoginActivity;
import dte.masteriot.mdp.suertees.IncidentLists.IncidentAdapter;
import dte.masteriot.mdp.suertees.Objects.Incident;
import dte.masteriot.mdp.suertees.R;

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

        // Fetch incidents when the activity is created
        fetchIncidents();

        Button logoutButton = findViewById(R.id.log_out_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the logout method when the button is clicked
                logout();
            }
        });

        // Register the back press callback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show the confirmation dialog here
                new AlertDialog.Builder(AdminActivity.this)
                        .setMessage("Are you sure you want to log out?")
                        .setCancelable(false)  // Prevent closing dialog by tapping outside
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Sign out from Firebase
                                FirebaseAuth.getInstance().signOut();
                                // Redirect to LoginActivity
                                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                                // Clear the activity stack and start the login activity
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Toast.makeText(AdminActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                finish(); // Finish the current activity
                            }
                        })
                        .setNegativeButton("No", null) // Dismiss the dialog on "No"
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
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

    public void logout() {
        // Create a confirmation dialog
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)  // Prevent closing dialog by tapping outside
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Sign out from Firebase
                        FirebaseAuth.getInstance().signOut();
                        // Redirect to a login activity
                        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                        // Clear the activity stack and start the login activity
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Toast.makeText(AdminActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null) // Dismiss the dialog on "No"
                .show();
    }

}
