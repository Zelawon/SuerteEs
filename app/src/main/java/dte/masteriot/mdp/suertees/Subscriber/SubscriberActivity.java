package dte.masteriot.mdp.suertees.Subscriber;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import java.util.ArrayList;

import dte.masteriot.mdp.suertees.R;

public class SubscriberActivity extends AppCompatActivity {

    private static final String TAG = "TAG_MDPMQTT";
    private static final String SERVER_HOST = "192.168.56.1"; // Broker IP
    private static final int SERVER_PORT = 1883; // Broker Port
    private static final String SUBSCRIPTION_TOPIC = "incidents/reports"; // Topic to subscribe to

    private Mqtt3AsyncClient mqttClient;
    private ListView messageListView; // ListView to display received messages
    private ArrayList<String> messageList; // List to hold all received messages
    private ArrayAdapter<String> messageAdapter; // Adapter to display messages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);

        // Initialize ListView and message list
        messageListView = findViewById(R.id.listViewMessages);
        messageList = new ArrayList<>();
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(messageAdapter);

        // Create and connect MQTT client
        createMQTTClient();
        connectToBroker();
    }

    private void createMQTTClient() {
        mqttClient = MqttClient.builder()
                .useMqttVersion3()
                .identifier("mqtt-subscriber-client")
                .serverHost(SERVER_HOST)
                .serverPort(SERVER_PORT)
                .buildAsync();
    }

    private void connectToBroker() {
        if (mqttClient != null) {
            mqttClient.connectWith()
                    .send()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            Log.d(TAG, "Problem connecting to server:");
                            Log.d(TAG, throwable.toString());
                        } else {
                            Log.d(TAG, "Connected to MQTT broker");
                            subscribeToTopic();
                        }
                    });
        } else {
            Log.d(TAG, "Cannot connect client (null)");
        }
    }

    private void subscribeToTopic() {
        if (mqttClient != null) {
            mqttClient.subscribeWith()
                    .topicFilter(SUBSCRIPTION_TOPIC)
                    .callback(this::onMessageReceived) // Handle incoming messages
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            Log.d(TAG, "Problem subscribing to topic:");
                            Log.d(TAG, throwable.toString());
                        } else {
                            Log.d(TAG, "Subscribed to topic: " + SUBSCRIPTION_TOPIC);
                        }
                    });
        }
    }

    // Callback method to handle incoming messages
    private void onMessageReceived(Mqtt3Publish publish) {
        String message = new String(publish.getPayloadAsBytes());
        Log.d(TAG, "Received message: " + message);

        // Add the new message to the message list and update the UI
        runOnUiThread(() -> {
            messageList.add(message); // Add the new message to the list
            messageAdapter.notifyDataSetChanged(); // Notify the adapter to update the ListView
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromBroker();
    }

    private void disconnectFromBroker() {
        if (mqttClient != null) {
            mqttClient.disconnect()
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            Log.d(TAG, "Problem disconnecting from server:");
                            Log.d(TAG, throwable.toString());
                        } else {
                            Log.d(TAG, "Disconnected from MQTT broker");
                        }
                    });
        }
    }
}
