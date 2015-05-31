package example.com.cominghome.background;

import android.app.Service;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import example.com.cominghome.app.App;
import example.com.cominghome.data.database.DBHelper;
import example.com.cominghome.data.database.DBManager;
import example.com.cominghome.data.database.RoutePoint;
import example.com.cominghome.data.database.RouteTable;

import static example.com.cominghome.app.App.TAG;
import static example.com.cominghome.app.App.getApp;
import static example.com.cominghome.utils.Utils.BTN_GO_HOME_STATE_KEY;
import static example.com.cominghome.utils.Utils.BTN_GO_STATE_KEY;
import static example.com.cominghome.utils.Utils.RECORD_MODE_KEY;
import static example.com.cominghome.utils.Utils.TURNING_MODE_KEY;
import static example.com.cominghome.utils.Utils.getAppPreferences;

public class LocationService extends Service {

    public static final String ACTION_START_RECORD = "ACTION_START_RECORD";
    public static final String ACTION_STOP_RECORD = "ACTION_STOP_RECORD";

    public static final String ACTION_LOCATION_WAS_FOUND = "ACTION_LOCATION_WAS_FOUND";

    public static final String ACTION_SEND_FIRST_POINT = "ACTION_SEND_FIRST_POINT";
    public static final String EXTRA_FIRST_POINT = "EXTRA_FIRST_POINT";

    public static final String ACTION_UPDATE_CURRENT_MARKER_LOCATION = "ACTION_UPDATE_CURRENT_MARKER_LOCATION";
    public static final String EXTRA_NEW_CURRENT_LOCATION = "EXTRA_UPDATED_CURRENT_LOCATION";


    private static boolean isRecordingMode = false;
    private boolean isFirstPoint = true;

    private Location lastLoc;
    private LocationManager manager;
    private LocationListener listener;

    private static float declination;

    private RouteTable routeTable;
    private int pointNumber = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service: onCreate");

        DBHelper helper = DBManager.getHelper();
        routeTable = helper.getRouteTable();

        isRecordingMode = getAppPreferences(this).getBoolean(RECORD_MODE_KEY, false);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocalLocationListener();

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, listener);
    }

    @Override
    public void onDestroy() {
        //Log.d(App.TAG, "Service: onDestroy");
        manager.removeUpdates(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(App.TAG, "Service: onStartCommand");

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
        } //else isFirstPoint = true;
        isRecordingMode = true;

        Log.d(TAG, "startRecord");

        getAppPreferences(this)
                .edit()
                .putBoolean(RECORD_MODE_KEY, true)
                .commit();

    }

    public void stopRecord() {
        isRecordingMode = false;
        Log.d(TAG, "stopRecord");

        getAppPreferences(this)
                .edit()
                .putBoolean(RECORD_MODE_KEY, false)
                .commit();
    }

    public static boolean isRecordingMode() {
        return isRecordingMode;
    }

    private void savePoint(Location loc) {
        if (!routeTable.contains(loc)) {
            routeTable.addRoutePoint(new RoutePoint(++pointNumber + "", loc.getLatitude() + "", loc.getLongitude() + ""));
        }
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
                            App.getApp(LocationService.this).getMe().getLongitude() != loc.getLongitude()) {
                App.getApp(LocationService.this).setMe(loc);

            }

            if (isRecordingMode) {
                savePoint(loc);
                if (isFirstPoint) {
                    sendBroadcast(new Intent(ACTION_SEND_FIRST_POINT).putExtra(EXTRA_FIRST_POINT, loc));
                    isFirstPoint = false;
                }
            } else {
                if (isFirstPoint) {
                    sendBroadcast(new Intent(ACTION_LOCATION_WAS_FOUND));
                    Toast.makeText(App.getApp(LocationService.this),
                            "My location has been found", Toast.LENGTH_LONG).show();
                }
            }

            lastLoc = loc;

            // for current marker location update
            if (!getAppPreferences(getApp(LocationService.this)).getBoolean(BTN_GO_STATE_KEY, true) ||
                    !getAppPreferences(getApp(LocationService.this)).getBoolean(BTN_GO_HOME_STATE_KEY, true)) {
                sendBroadcast(new Intent(ACTION_UPDATE_CURRENT_MARKER_LOCATION)
                        .putExtra(EXTRA_NEW_CURRENT_LOCATION, loc));
            }

            // for map rotation
            if (getAppPreferences(LocationService.this).getBoolean(TURNING_MODE_KEY, false)) {
                GeomagneticField field = new GeomagneticField(
                        (float) loc.getLatitude(),
                        (float) loc.getLongitude(),
                        (float) loc.getAltitude(),
                        System.currentTimeMillis()
                );

                // getDeclination returns degrees
                declination = field.getDeclination();
            }
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

    public static float getDeclination() {
        return declination;
    }
}