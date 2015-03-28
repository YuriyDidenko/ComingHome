package example.com.cominghome;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
    private Location me;
    private LocationManager manager;
    private LocationListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(MapsActivity.TAG, "Service: onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MapsActivity.TAG, "Service: onDestroy");
        manager.removeUpdates(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MapsActivity.TAG, "Service: onStartCommand");
        findMe();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void findMe() {
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (me != null)
                    if (location.getLatitude() == me.getLatitude() && location.getLongitude() == me.getLongitude())
                        return;
                me = location;
                Log.d(MapsActivity.TAG, me.toString());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }
}