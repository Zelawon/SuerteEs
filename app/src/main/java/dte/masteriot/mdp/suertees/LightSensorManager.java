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
import androidx.lifecycle.MutableLiveData;

public class LightSensorManager {

    private static final String TAG = "LightSensorManager";
    private static LightSensorManager instance;
    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private final MutableLiveData<Boolean> isDarkModeLiveData = new MutableLiveData<>();
    private static final float DARK_THRESHOLD = 30.0f; // Lux threshold for dark mode
    private static final long DARK_MODE_DELAY = 3000; // seconds delay
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

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float lux = event.values[0];
                Log.d(TAG, "Light sensor value (lux): " + lux);

                if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO){
                    isDarkMode = false;
                }

                // Determine if dark mode should be enabled
                if (lux < DARK_THRESHOLD) {
                    if (!isDarkMode) {
                        // Schedule dark mode notification
                        if (darkModeRunnable == null) {
                            darkModeRunnable = () -> {
                                isDarkMode = true;
                                isDarkModeLiveData.setValue(true);
                                if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                                    Toast.makeText(context, "Ambient light is low. Switching to Dark Mode may improve visibility.", Toast.LENGTH_LONG).show();
                                }
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                Log.d(TAG, "Dark mode activated.");
                            };
                        }
                        handler.postDelayed(darkModeRunnable, DARK_MODE_DELAY);
                    }
                } else {
                    // Cancel dark mode notification if light goes above threshold
                    if (darkModeRunnable != null) {
                        handler.removeCallbacks(darkModeRunnable);
                        darkModeRunnable = null;
                    }

                    if (isDarkMode) {
                        isDarkMode = false;
                        isDarkModeLiveData.setValue(false);
                        Log.d(TAG, "Dark mode deactivated.");
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
