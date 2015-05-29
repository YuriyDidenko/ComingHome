package example.com.cominghome.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.google.android.gms.maps.model.Marker;

public class MarkerRotateSensorListener implements SensorEventListener {
    private Marker curLocMarker;

    public MarkerRotateSensorListener(Marker curLocMarker) {
        this.curLocMarker = curLocMarker;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (curLocMarker != null)
            curLocMarker.setRotation(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}