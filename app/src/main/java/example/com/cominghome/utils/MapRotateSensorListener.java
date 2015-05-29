package example.com.cominghome.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

import example.com.cominghome.background.LocationService;

public class MapRotateSensorListener implements SensorEventListener {
    private GoogleMap mMap;

    public MapRotateSensorListener(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mRotationMatrix = new float[16];
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            float bearing = (float) Math.toDegrees(orientation[0]) + LocationService.getDeclination();
            updateCamera(bearing);
        }
    }

    private void updateCamera(float bearing) {
        CameraPosition oldPos = mMap.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}