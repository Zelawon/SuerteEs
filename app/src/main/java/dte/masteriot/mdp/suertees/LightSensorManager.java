package dte.masteriot.mdp.suertees;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

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
    private Context context; // Context for showing Toast messages


    private LightSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.context = context;
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
