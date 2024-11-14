package dte.masteriot.mdp.suertees;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

public class LightSensorManager {

    private static final String TAG = "LightSensorManager";
    private static LightSensorManager instance;
    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private final MutableLiveData<Boolean> isDarkModeLiveData = new MutableLiveData<>();
    private static final float DARK_THRESHOLD = 30.0f; // Lux threshold for dark mode
    private final Context context; // Context for showing Toast messages
    private boolean isListening = false;

    private LightSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.context = context;
    }

    public static synchronized LightSensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new LightSensorManager(context.getApplicationContext()); // Use app context to prevent leaks
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

                // Determine if dark mode should be enabled
                boolean newDarkMode = lux < DARK_THRESHOLD;
                if (isDarkModeLiveData.getValue() == null || isDarkModeLiveData.getValue() != newDarkMode) {
                    isDarkModeLiveData.setValue(newDarkMode);

                    // Show a Toast message when dark mode changes
                    if(newDarkMode){
                        Toast.makeText(context, "Consider Changing to Dark Mode", Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, "Light sensor stopped listening and listener unregistered.");
        }
    }
}
