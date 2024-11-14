package dte.masteriot.mdp.suertees;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LightSensorManager {

    private static final String TAG = "LightSensorManager";
    private static LightSensorManager instance;
    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private MutableLiveData<Boolean> isDarkModeLiveData = new MutableLiveData<>();
    private static final float DARK_THRESHOLD = 30.0f; // Lux threshold for dark mode

    private LightSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public static LightSensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new LightSensorManager(context);
        }
        return instance;
    }

    public void startListening() {
        if (lightSensor == null) {
            Log.w(TAG, "No light sensor found.");
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
                    Log.d(TAG, "Is dark mode: " + newDarkMode);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // No implementation needed for this example
            }
        };

        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopListening() {
        if (lightSensorListener != null) {
            sensorManager.unregisterListener(lightSensorListener);
        }
    }

    // Returns LiveData that can be observed for dark mode changes
    public LiveData<Boolean> isDarkMode() {
        return isDarkModeLiveData;
    }
}
