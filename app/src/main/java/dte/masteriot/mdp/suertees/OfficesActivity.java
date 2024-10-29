package dte.masteriot.mdp.suertees;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class OfficesActivity extends AppCompatActivity {
    public static final String LOADWEBTAG = "LOAD_WEB_TAG"; // to easily filter logs
    private static final String URL_OFFICES = "https://datos.madrid.es/egob/catalogo/200149-0-oficinas-linea-madrid.json";
    private static final String CONTENT_TYPE_JSON = "application/json";
    ExecutorService es;
    private String threadAndClass; // to clearly identify logs
    private Button btBack;
    private TextView text;
    // Define the handler that will receive the messages from the background thread:
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // message received from background thread: load complete (or failure)
            String string_result;
            super.handleMessage(msg);
            Log.d(LOADWEBTAG, threadAndClass + ": message received from background thread");
            if ((string_result = msg.getData().getString("text")) != null) {
                text.setText(string_result);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offices);

        // Build the logTag with the Thread and Class names:
        threadAndClass = "Thread = " + Thread.currentThread().getName() + ", Class = " +
                this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);

        // Get references to UI elements:
        btBack = findViewById(R.id.buttonBack);
        text = findViewById(R.id.HTTPTextView);

        //  Set initial text:
        text.setText("Click button to load the contents of " + URL_OFFICES);

        // Create an executor for the background tasks:
        es = Executors.newSingleThreadExecutor();

        text.setText("Loading " + URL_OFFICES + "..."); // Inform the user by means of the TextView

        // Execute the loading task in background:
        LoadURLContents loadURLContents = new LoadURLContents(handler, CONTENT_TYPE_JSON, URL_OFFICES);
        es.execute(loadURLContents);
    }

    // Listener for the button:
    public void goBack(View view) {
        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(OfficesActivity.this, HomeActivity.class);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);
    }


}