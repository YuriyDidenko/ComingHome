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

    public static final String ACTION_START_RECORD = "ACTION_START_RECORD";
    public static final String ACTION_STOP_RECORD = "ACTION_STOP_RECORD";
    public static final String ACTION_SEND_FIRST_POINT = "ACTION_SEND_FIRST_POINT";
    public static final String EXTRA_FIRST_POINT = "EXTRA_FIRST_POINT";

    private static boolean isRecordingMode = false;
    private boolean isFirstPoint;

    private Location lastLoc;
    private LocationManager manager;
    private LocationListener listener;

    private RouteTable routeTable;
    private int pointNumber = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(App.TAG, "Service: onCreate");

        DBHelper helper = DBManager.getHelper();
        routeTable = helper.getRouteTable();

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocalLocationListener();

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }

    @Override
    public void onDestroy() {
        //Log.d(App.TAG, "Service: onDestroy");
        manager.removeUpdates(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(App.TAG, "Service: onStartCommand");
//        Log.d(App.TAG, "Intent " + intent.toString());
//        Log.d(App.TAG, "flags " + flags);
//        Log.d(App.TAG, "startId " + startId);

        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_START_RECORD:
                    startRecord();
                    break;
                case ACTION_STOP_RECORD:
                default:
                    stopRecord();
            }
        }
        return START_NOT_STICKY;
    }

    public void startRecord() {
        if (lastLoc != null) {
            sendBroadcast(new Intent(ACTION_SEND_FIRST_POINT).putExtra(EXTRA_FIRST_POINT, lastLoc));
            savePoint(lastLoc);
        } else isFirstPoint = true;
        isRecordingMode = true;
    }

    public void stopRecord() {
        isRecordingMode = false;
    }

    private void savePoint(Location loc) {
        if (!routeTable.contains(loc))
            routeTable.addRoutePoint(new RoutePoint(++pointNumber + "", loc.getLatitude() + "", loc.getLongitude() + ""));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class LocalLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (lastLoc != null && lastLoc.getLatitude() == loc.getLatitude() && lastLoc.getLongitude() == loc.getLongitude())
                return;

            Log.d(App.TAG, "Location has been updated");

            if (App.getApp(LocationService.this).getMe() == null ||
                    (App.getApp(LocationService.this).getMe().getLatitude() != loc.getLatitude()) &&
                            App.getApp(LocationService.this).getMe().getLongitude() != loc.getLongitude())
                App.getApp(LocationService.this).setMe(loc);


            if (isRecordingMode) {
                savePoint(loc);
                if (isFirstPoint) {
                    sendBroadcast(new Intent(ACTION_SEND_FIRST_POINT).putExtra(EXTRA_FIRST_POINT, loc));
                    isFirstPoint = false;
                }
            }
            lastLoc = loc;
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
}