package example.com.cominghome.background;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import example.com.cominghome.app.App;
import example.com.cominghome.data.DBHelper;
import example.com.cominghome.data.DBManager;
import example.com.cominghome.data.RoutePoint;
import example.com.cominghome.data.RouteTable;

public class LocationService extends Service {
    private static boolean recording = false;

    private Location me;
    private LocationManager manager;
    private LocationListener listener;

    private RouteTable routeTable;
    private int pointNumber;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(App.TAG, "Service: onCreate");

        DBHelper helper = DBManager.getHelper();
        routeTable = helper.getRouteTable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(App.TAG, "Service: onDestroy");
        manager.removeUpdates(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(App.TAG, "Service: onStartCommand");
        findMe();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void findMe() {
        pointNumber = 1;
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // старая точка
                if (me != null)
                    if (location.getLatitude() == me.getLatitude() && location.getLongitude() == me.getLongitude())
                        return;
                me = location;
                Log.d(App.TAG, me.toString());

                if (recording) {
                    routeTable.addRoutePoint(new RoutePoint(
                            pointNumber++ + "", me.getLatitude() + "", me.getLongitude() + ""));
                } else //if (MapsActivity.isActivated)
                    App.setMe(me);
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
        }

        ;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }

    private void test() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(App.TAG, "test");
            }
        }).start();

    }

    public static void startRecord() {
        recording = true;
    }

    public static void stopRecord() {
        recording = false;
    }
}