package dte.masteriot.mdp.suertees;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
public class LightSensorManager {

    private static final String TAG = "LightSensorManager";
    private static LightSensorManager instance;
    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private static final float DARK_THRESHOLD = 30.0f; // Lux threshold for dark mode
    private static final long DARK_MODE_DELAY = 5000; // seconds delay
    private final Context context;
    private boolean isListening = false;
    private boolean isDarkMode = false; // Tracks current dark mode state
    private int lastMode; // Track the last known mode

    long DARK_MODE_DELAY_1 = 5000; // 5 seconds delay for the first notification

    private Handler handler; // Handler to manage delayed notifications
    private Runnable darkModeRunnable; // Runnable to notify user
    private boolean notificationSent = false; // Tracks if notification is sent

    private LightSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public static synchronized LightSensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new LightSensorManager(context.getApplicationContext());
        }
        return instance;
    }

    public void startListening() {
        if (lightSensor == null) {
            Log.w(TAG, "No light sensor found.");
            return;
        }

        if (isListening) {
            Log.d(TAG, "Light sensor is already listening.");
            return;
        }

        lastMode = AppCompatDelegate.getDefaultNightMode();

        lightSensorListener = new SensorEventListener() {
            private float currentLux = 0;

            @Override
            public void onSensorChanged(SensorEvent event) {
                checkForModeChange(); // Check if the user toggled dark mode

                currentLux = event.values[0];
                Log.d(TAG, "Light sensor value (lux): " + currentLux);

                isDarkMode = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO;

                if (currentLux < DARK_THRESHOLD && !isDarkMode) {
                    // Only show the toast if it hasn't been shown already
                    if (!notificationSent) {
                        scheduleNotification(DARK_MODE_DELAY_1, currentLux);
                    }
                } else {
                    if (currentLux >= DARK_THRESHOLD) {
                        cancelAllNotifications();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // No implementation needed
            }
        };

        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        isListening = true;
        Log.d(TAG, "Light sensor started listening.");
    }

    private void scheduleNotification(long delay, float luxAtScheduling) {
        if (darkModeRunnable != null) {
            Log.d(TAG, "Notification already scheduled. Skipping...");
            return;
        }

        Log.d(TAG, "Scheduling dark mode notification...");
        darkModeRunnable = () -> {
            if (luxAtScheduling < DARK_THRESHOLD) { // Use captured value
                // Show the toast message
                Toast.makeText(context, "Ambient light is low. Switching to Dark Mode may improve visibility.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Dark mode notification sent.");
                notificationSent = true; // Mark that a notification has been sent
            }
            darkModeRunnable = null; // Reset after execution
        };
        handler.postDelayed(darkModeRunnable, delay);
    }

    private void cancelAllNotifications() {
        Log.d(TAG, "Canceling all dark mode notifications...");
        if (darkModeRunnable != null) {
            handler.removeCallbacks(darkModeRunnable);
            darkModeRunnable = null;
        }
        notificationSent = false; // Reset the notification flag
    }

    private void checkForModeChange() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode != lastMode) {
            Log.d(TAG, "Detected manual mode change. Resetting notification flag.");
            resetNotificationFlag();
            lastMode = currentMode; // Update the last known mode
        }
    }

    private void resetNotificationFlag() {
        notificationSent = false;
        if (darkModeRunnable != null) {
            handler.removeCallbacks(darkModeRunnable);
            darkModeRunnable = null;
        }
        Log.d(TAG, "Notification flag reset.");
    }

    public void stopListening() {
        if (isListening && lightSensorListener != null) {
            sensorManager.unregisterListener(lightSensorListener);
            lightSensorListener = null;
            isListening = false;

            // Cancel any pending notifications
            cancelAllNotifications();

            Log.d(TAG, "Light sensor stopped listening and listener unregistered.");
        }
    }
}

