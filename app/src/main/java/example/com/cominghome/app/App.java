package example.com.cominghome.app;

import android.app.Application;
import android.location.Location;

import example.com.cominghome.data.DBManager;

public class App extends Application {
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
        return me;
    }

    public static void setMe(Location me) {
        App.me = me;
    }
}
