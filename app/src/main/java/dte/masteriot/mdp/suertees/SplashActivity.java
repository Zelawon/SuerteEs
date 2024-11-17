package dte.masteriot.mdp.suertees;

import android.app.UiModeManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dte.masteriot.mdp.suertees.AccountManagment.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1000; // Splash screen time
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the UiModeManager system service
        UiModeManager uiModeManager = (UiModeManager) getSystemService(this.UI_MODE_SERVICE);

        // Check the system's night mode setting
        int currentNightMode = uiModeManager.getNightMode();

        // Set the app's default night mode based on the system's night mode
        if (currentNightMode == UiModeManager.MODE_NIGHT_YES) {
            // System is in dark mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // System is in light mode or undefined (if undefined, default to light mode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Apply the night mode setting immediately
        getDelegate().applyDayNight();
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