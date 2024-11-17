package dte.masteriot.mdp.suertees.IncidentLists;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import dte.masteriot.mdp.suertees.Objects.Incident;
import dte.masteriot.mdp.suertees.R;

public class IncidentDetailActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private TextView textViewActualTitle;
    private TextView textViewActualDesc;
    private TextView textViewActualDate;
    private TextView textViewActualLocation;
    private TextView textViewActualType;
    private TextView textViewActualUrgency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_detail); // Create a corresponding layout

        // Initialize views
        textViewActualTitle = findViewById(R.id.textViewActualTitle);
        textViewActualDesc = findViewById(R.id.textViewActualDesc);
        textViewActualDate = findViewById(R.id.textViewActualDate);
        textViewActualLocation = findViewById(R.id.textViewActualLocation);
        textViewActualType = findViewById(R.id.textViewActualType);
        textViewActualUrgency = findViewById(R.id.textViewActualUrgency);

        firestore = FirebaseFirestore.getInstance();

        // Retrieve the incident data from Intent
        String incidentId = getIntent().getStringExtra("incidentId");
        if (incidentId != null) {
            retrieveIncident(incidentId);
        } else {
            Toast.makeText(this, "Incident ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void retrieveIncident(String incidentId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference incidentRefUserId = firestore.collection("incidents")
                .document(userId)
                .collection("userIdIncidents")
                .document(incidentId);

        incidentRefUserId.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Incident incident = documentSnapshot.toObject(Incident.class);
                            // Fill the views with incident data
                            if (incident != null) {
                                updateIncidentViews(incident);
                                // Show delete button if userId matches
                                findViewById(R.id.buttonDelete).setVisibility(View.VISIBLE);
                            }
                        } else {
                            // If the Incident does not belong to the user
                            // Attempt to retrieve from the "allIncidents" collection
                            retrieveFromAllIncidents(incidentId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(IncidentDetailActivity.this, "Error retrieving incident: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void retrieveFromAllIncidents(String incidentId) {
        DocumentReference incidentRefAll = firestore.collection("incidents")
                .document("allIncidents")
                .collection("Incidents")
                .document(incidentId);

        incidentRefAll.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Incident incident = documentSnapshot.toObject(Incident.class);
                            // Fill the views with incident data
                            if (incident != null) {
                                updateIncidentViews(incident);
                                // Hide delete button as it's not owned by the user
                                findViewById(R.id.buttonDelete).setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(IncidentDetailActivity.this, "Incident not found in database", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(IncidentDetailActivity.this, "Error retrieving incident from all incidents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateIncidentViews(Incident incident) {
        textViewActualTitle.setText(incident.getTitle());
        textViewActualDesc.setText(incident.getDescription());
        textViewActualDate.setText(incident.getDate());
        textViewActualLocation.setText(incident.getLocation());
        textViewActualType.setText(incident.getType());
        textViewActualUrgency.setText(incident.getUrgency());
    }

    public void goBack(View v) {
        finish();
    }

    public void deleteItem(View v) {
        String incidentId = getIntent().getStringExtra("incidentId");
        if (incidentId != null) {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this incident?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Show a progress dialog while deleting
                        ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setMessage("Deleting incident...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        // Delete the incident from the user's collection
                        DocumentReference incidentRefUserId = firestore.collection("incidents")
                                .document(userId)
                                .collection("userIdIncidents")
                                .document(incidentId);

                        incidentRefUserId.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Delete from the all incidents collection
                                        DocumentReference incidentRefAll = firestore.collection("incidents")
                                                .document("allIncidents")
                                                .collection("Incidents")
                                                .document(incidentId);

                                        incidentRefAll.delete()
                                                .addOnCompleteListener(allTask -> {
                                                    progressDialog.dismiss();
                                                    if (allTask.isSuccessful()) {
                                                        Toast.makeText(IncidentDetailActivity.this, "Incident deleted successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(IncidentDetailActivity.this, "Error deleting incident from all incidents: " + allTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(IncidentDetailActivity.this, "Error deleting incident from user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            Toast.makeText(this, "Error: Incident ID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}