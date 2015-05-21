package example.com.cominghome.utils;

import example.com.cominghome.data.DBManager;
import example.com.cominghome.data.RouteTable;

/**
 * Created by Loner on 21.05.2015.
 */
public class Utils {
    public static final String SHARED_PREFERENCES_NAME = "prefs";
    public static final String BTN_GO_STATE_KEY = "1 enabled";
    public static final String BTN_GO_HOME_STATE_KEY = "2 enabled";
    public static final String ZOOM_KEY = "zoom";


    private static RouteTable routeTable = DBManager.getHelper().getRouteTable();

    // сюда надо будет попробовать впилить шаред префс и прочие дополнительные методы и выпилить из MapsActivity
    //private static Context context;

//    // shared prefs
//    private static void saveMapState(Context context, boolean btnGoState, boolean btnGoHomeState, float zoom) {
//        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//
//        editor.putBoolean(BTN_GO_STATE_KEY, btnGoState/*btnGo.isEnabled()*/);
//        editor.putBoolean(BTN_GO_HOME_STATE_KEY, btnGoHomeState/*btnGoHome.isEnabled()*/);
//        editor.putFloat(ZOOM_KEY, zoom);
//        editor.commit();
//
//        //Log.d(App.TAG, "data was saved: 1 enabled - " + btnGo.isEnabled() + ", 2 enabled - " + btnGoHome.isEnabled());
//    }
//
//    private static void loadMapState(Context context, GoogleMap mMap,  Button btnGo, Button btnGoHome,
//                                     RouteTable routeTable, Marker beginLocation, Marker endLocation) {
//        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
//        boolean isGoEnabled, isGoHomeEnabled;
//        isGoEnabled = prefs.getBoolean(BTN_GO_STATE_KEY, true);
//        isGoHomeEnabled = prefs.getBoolean(BTN_GO_HOME_STATE_KEY, true);
//
//        if (!isGoEnabled) {
//            LatLng latlngBegin = new LatLng(
//                    Double.parseDouble(routeTable.getFirstLocation().getLatitude()),
//                    Double.parseDouble(routeTable.getFirstLocation().getLongtitude()));
//            beginLocation = mMap.addMarker(new MarkerOptions().position(latlngBegin).title("start")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//            btnGo.setEnabled(false);
//        }
//        if (!isGoHomeEnabled) {
//            LatLng latlngEnd = new LatLng(
//                    Double.parseDouble(routeTable.getLastLocation().getLatitude()),
//                    Double.parseDouble(routeTable.getLastLocation().getLongtitude()));
//            endLocation = mMap.addMarker(new MarkerOptions().position(latlngEnd).title("end"));
//            btnGoHome.setEnabled(false);
//        }
//
//        //Log.d(App.TAG, "data was loaded: 1 enabled - " + isGoEnabled + ", 2 enabled - " + isGoHomeEnabled);
//    }
//
//    private static void removeSharedPreferences(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.remove(BTN_GO_STATE_KEY);
//        editor.remove(BTN_GO_HOME_STATE_KEY);
//        editor.remove(ZOOM_KEY);
//        editor.clear();
//        editor.commit();
//    }
//
}
