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

import java.util.concurrent.atomic.AtomicBoolean;

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
    private Handler handler; // Handler to manage delayed notifications
    private Runnable darkModeRunnable; // Runnable to notify user

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

        long DARK_MODE_DELAY_1 = 5000; // seconds delay for the first notification
        long DARK_MODE_DELAY_2 = 20000; // seconds delay for the second notification
        AtomicBoolean firstNotificationSent = new AtomicBoolean(false); // Tracks if the first notification is sent
        AtomicBoolean secondNotificationSent = new AtomicBoolean(false); // Tracks if the second notification is sent

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float lux = event.values[0];
                Log.d(TAG, "Light sensor value (lux): " + lux);

                isDarkMode = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO;

                if (lux < DARK_THRESHOLD && !isDarkMode) {
                    // Entering a dark environment
                    if (!firstNotificationSent.get()) {
                        // Schedule first notification
                        handler.postDelayed(() -> {
                            if (lux < DARK_THRESHOLD) { // Check if still in dark
                                Toast.makeText(context, "Ambient light is low. Switching to Dark Mode may improve visibility.", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "First dark mode notification sent.");
                                firstNotificationSent.set(true);
                            }
                        }, DARK_MODE_DELAY_1);
                    }

                    if (firstNotificationSent.get() && !secondNotificationSent.get()) {
                        // Schedule second notification
                        handler.postDelayed(() -> {
                            if (lux < DARK_THRESHOLD) { // Check if still in dark
                                Toast.makeText(context, "It's still dark. Consider switching to Dark Mode.", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Second dark mode notification sent.");
                                secondNotificationSent.set(true);
                            }
                        }, DARK_MODE_DELAY_2);
                    }
                } else {
                    // Reset notifications when light environment is detected
                    if (lux >= DARK_THRESHOLD) {
                        firstNotificationSent.set(false);
                        secondNotificationSent.set(false);
                        Log.d(TAG, "Light environment detected. Notifications reset.");
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // No implementation needed for this example
            }
        };

        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        isListening = true;
        Log.d(TAG, "Light sensor started listening.");
    }


    public void stopListening() {
        if (isListening && lightSensorListener != null) {
            sensorManager.unregisterListener(lightSensorListener);
            lightSensorListener = null;
            isListening = false;

            // Cancel any pending notifications
            if (darkModeRunnable != null) {
                handler.removeCallbacks(darkModeRunnable);
                darkModeRunnable = null;
            }

            Log.d(TAG, "Light sensor stopped listening and listener unregistered.");
        }
    }
}
