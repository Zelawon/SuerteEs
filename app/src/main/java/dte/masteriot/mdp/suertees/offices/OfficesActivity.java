package dte.masteriot.mdp.suertees.offices;

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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dte.masteriot.mdp.suertees.HomeActivity;
import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.offices.model.DatasetOffices;
import dte.masteriot.mdp.suertees.offices.adapter.MyAdapter;


public class OfficesActivity extends AppCompatActivity implements OnDataLoadedListener {
    public static final String LOADWEBTAG = "LOAD_WEB_TAG"; // to easily filter logs
    private static final String URL_OFFICES = "https://datos.madrid.es/egob/catalogo/200149-0-oficinas-linea-madrid.json";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private ExecutorService es;
    private Button btBack;
    private TextView text;

    private DatasetOffices dataset;
    private RecyclerView recyclerView;

    // Define the handler that will receive the messages from the background thread:
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // message received from background thread: load complete (or failure)
            super.handleMessage(msg);
            String string_result = msg.getData().getString("text");
            if (string_result != null) {
                // Call the onDataLoaded callback with the loaded data
                onDataLoaded(string_result);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offices);

        // Get references to UI elements:
        btBack = findViewById(R.id.buttonBack);
        text = findViewById(R.id.HTTPTextView);

        // Create an executor for the background tasks:
        es = Executors.newSingleThreadExecutor();

        // Inform the user:
        text.setText("Loading " + URL_OFFICES + "...");

        // Execute the loading task in background:
        LoadURLContents loadURLContents = new LoadURLContents(handler, CONTENT_TYPE_JSON, URL_OFFICES);
        es.execute(loadURLContents);
    }

    // Listener for the button:
    public void goBack(View view) {
        // Creating Intent For Navigating to HomeActivity
        Intent i = new Intent(OfficesActivity.this, HomeActivity.class);
        startActivity(i);
    }

    @Override
    public void onDataLoaded(String data) {
        // This method is called after data is loaded
        text.setText(null);

        Log.d(LOADWEBTAG, "Data loaded: " + data);

        dataset = new DatasetOffices(this, data); // Initialize here

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        MyAdapter recyclerViewAdapter = new MyAdapter(dataset);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager to be set.
        // some options for the layout manager:  GridLayoutManager, LinearLayoutManager, StaggeredGridLayoutManager
        // by default, a linear layout is chosen:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}