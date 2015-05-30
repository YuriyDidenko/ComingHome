package example.com.cominghome.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.util.Log;

import example.com.cominghome.data.database.DBManager;

public class App extends Application {
    public static final String TAG = "logs";
    private Location me;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application: onCreate");
        DBManager.initHelper(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "Application: onTerminate");
        DBManager.closeHelper();
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(App.TAG, "Application: OnCongirurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    public Location getMe() {
        if (me != null) {
            return me;
        }
        return null;
    }

    public void setMe(Location location) {
        if (me != null && location != null)
            // если инфа такая же, как и была
            if (me.getLatitude() == location.getLatitude() && me.getLongitude() == location.getLongitude())
                return;
        if (location == null) {
            Log.d(TAG, "App: setMe() location == null");
            return;
        }
        if (me == null) {
            me = new Location(location);
            //Log.d(TAG, "App: setMe() me was null; updated, me = " + me + " getMe() = " + getMe());
        } else {
            me.setLatitude(location.getLatitude());
            me.setLongitude(location.getLongitude());
            //Log.d(TAG, "App: setMe() me wasn't null; my location has been updated");
        }
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static App getApp(Context ctx) {
        if (ctx instanceof App) {
            return (App) ctx;
        }
        return (App) ctx.getApplicationContext();
    }

}
