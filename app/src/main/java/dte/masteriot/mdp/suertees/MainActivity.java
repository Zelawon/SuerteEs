package dte.masteriot.mdp.suertees;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dte.masteriot.mdp.suertees.accountmanagment.LoginActivity;
import dte.masteriot.mdp.suertees.accountmanagment.SignupActivity;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1000; // Splash screen time
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the app to always use night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Display splash screen for SPLASH_DELAY milliseconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is signed in (non-null) and update UI accordingly.
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is signed in, go to the start activity
                    //startActivity(new Intent(MainActivity.this, HomeActivity.class));

                    //TO-REMOVE Later and Change Logic Back
                    // If user is logged in, sign them out & Redirect to login page
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    // No user is signed in, go to the login page
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, SPLASH_DELAY);
    }
}