package example.com.cominghome.app;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import example.com.cominghome.data.DBManager;

public class App extends Application {
    public static final String TAG = "log";
    private static Location me;

    @Override
    public void onCreate() {
        super.onCreate();
        DBManager.initHelper(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DBManager.closeHelper();
    }

    public static Location getMe() {
        if (me != null) {
            return me;
        }
        Log.d(TAG, "App: getMe() == null");
        return null;
    }

    public static void setMe(Location location) {
        // если инфа такая же, как и была
        if (me != null &&
                me.getLatitude() == location.getLatitude() &&
                me.getLongitude() == location.getLongitude())
            return;
        if (location == null) {
            Log.d(TAG, "App: setMe() == null");
            return;
        }
        Log.d(TAG, "App: setMe() my location has been updated");
        me = location;
    }
}
