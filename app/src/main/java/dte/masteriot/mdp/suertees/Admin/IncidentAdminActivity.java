package dte.masteriot.mdp.suertees.Admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
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
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import dte.masteriot.mdp.suertees.MenuAction;
import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.accountmanagment.LoginActivity;
import dte.masteriot.mdp.suertees.objects.Incident;

public class IncidentAdminActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private TextView textViewActualTitle;
    private TextView textViewActualDesc;
    private TextView textViewActualDate;
    private TextView textViewActualLocation;
    private TextView textViewActualType;
    private TextView textViewActualUrgency;
    private Incident inci;

    String TAG = "TAG_MDPMQTT";
    String serverHost = "192.168.56.1";  // MQTT server host
    int serverPort = 1883;               // MQTT server port
    Mqtt3AsyncClient client;
    private boolean isConnected = false;  // Track connection status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_admin);

        // Initialize views
        textViewActualTitle = findViewById(R.id.textViewActualTitle);
        textViewActualDesc = findViewById(R.id.textViewActualDesc);
        textViewActualDate = findViewById(R.id.textViewActualDate);
        textViewActualLocation = findViewById(R.id.textViewActualLocation);
        textViewActualType = findViewById(R.id.textViewActualType);
        textViewActualUrgency = findViewById(R.id.textViewActualUrgency);

        firestore = FirebaseFirestore.getInstance();
        createMQTTclient();
        connectToBroker();

        // Retrieve the incident data from Intent
        String incidentId = getIntent().getStringExtra("incidentId");
        if (incidentId != null) {
            retrieveFromAllIncidents(incidentId);
        } else {
            Toast.makeText(this, "Incident ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    void connectToBroker() {
        if (client != null) {
            client.connectWith()
                    .send()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            // handle failure
                            Log.d(TAG, "Problem connecting to server:");
                            Log.d(TAG, throwable.toString());
                            isConnected = false;  // Set to false if connection fails
                        } else {
                            // connected -> setup subscribes and publish a message
                            Log.d(TAG, "Connected to server");
                            isConnected = true;  // Set to true when connected
                        }
                    });
        } else {
            Log.d(TAG, "Cannot connect client (null)");
        }
    }

    void createMQTTclient() {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("my-mqtt-client-id")
                .serverHost(serverHost)
                .serverPort(serverPort)
                .buildAsync();
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
                                inci = incident;
                                updateIncidentViews(incident);
                            }
                        } else {
                            Toast.makeText(IncidentAdminActivity.this, "Incident not found in database", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(IncidentAdminActivity.this, "Error retrieving incident from all incidents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        String userId = inci.getUserID();
        if (incidentId != null) {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Mark as Resolved")
                    .setMessage("Are you sure you want to mark the incident as resolved?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Show a progress dialog while deleting
                        ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setMessage("Marking incident as done...");
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
                                                        // Publish the MQTT message here after deleting
                                                        publishIncidentDoneMessage();

                                                        Toast.makeText(IncidentAdminActivity.this, "Incident marked as done successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(IncidentAdminActivity.this, "Error marking as done: " + allTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(IncidentAdminActivity.this, "Error marking as done: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    // Method to publish MQTT message when the incident is marked as done
    private void publishIncidentDoneMessage() {
        String topic = "incidents";  // Set the topic to track the status
        String message = "ID:"+inci.getUserID()+"Incident: " + inci.getTitle() + " Added on: " + inci.getDate() + " has been resolved!";  // Message to publish indicating incident is done

        if (isConnected) {  // Check connection state using the boolean flag
            client.publishWith()
                    .topic(topic)
                    .payload(message.getBytes())  // Payload as byte array
                    .send()
                    .whenComplete((publish, throwable) -> {
                        if (throwable != null) {
                            // handle failure to publish
                            Log.d(TAG, "Problem publishing on topic:");
                            Log.d(TAG, throwable.toString());
                        } else {
                            // handle successful publish
                            Log.d(TAG, "Message published: " + message);
                        }
                    });
        } else {
            Log.d(TAG, "MQTT client not connected, message not sent.");
        }
    }

    // Method to handle toolbar icon click
    public void onMenuIconClick(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_logout, popupMenu.getMenu());

        // Set a listener for menu item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            MenuAction action = MenuAction.fromId(item.getItemId());
            if (action != null) {
                if (action == MenuAction.LOGOUT) {
                    logout();
                    return true;
                }
            }
            return false;
        });

        popupMenu.show(); // Show the popup menu
    }

    private void logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();
        // Redirect to a login activity
        Intent intent = new Intent(IncidentAdminActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }
}
