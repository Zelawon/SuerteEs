package dte.masteriot.mdp.suertees;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dte.masteriot.mdp.suertees.accountmanagment.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1000; // Splash screen time
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Display splash screen for SPLASH_DELAY milliseconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is signed in (non-null) and update UI accordingly.
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
////                     User is signed in, go to the start activity
//                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));

                    //TO-REMOVE Later and Change Logic Back
                    // If user is logged in, sign them out & Redirect to login page
                    mAuth.signOut();
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                } else {
                    // No user is signed in, go to the login page
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, SPLASH_DELAY);
    }

}