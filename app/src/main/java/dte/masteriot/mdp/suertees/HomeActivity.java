package dte.masteriot.mdp.suertees;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
    }

    public void reportIssue(View v) {

        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(HomeActivity.this, ReportIncidentActivity.class);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);

    }

    public void seeAll(View v) {
        /*
        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(HomeActivity.this, ReportIncidentActivity.class);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);
        */

    }

    public void seeMine(View v) {
        /*
        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(HomeActivity.this, ReportIncidentActivity.class);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);
        */
    }

    public void seeOffices(View v) {

        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(HomeActivity.this, OfficesActivity.class);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);

    }
}