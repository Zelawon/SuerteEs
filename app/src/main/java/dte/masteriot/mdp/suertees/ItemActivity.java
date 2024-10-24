package dte.masteriot.mdp.suertees;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item);
    }

    public void goBack(View v) {

        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(ItemActivity.this, HomeActivity.class);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);

    }

    public void deleteItem(View v) {
        //////delete implementation

        // Creating Intent For Navigating to Second Activity (Explicit Intent)
        Intent i = new Intent(ItemActivity.this, HomeActivity.class);

        // Once the intent is parametrized, start the second activity:
        startActivity(i);

    }
}