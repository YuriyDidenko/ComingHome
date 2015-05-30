package example.com.cominghome.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import example.com.cominghome.data.database.DBManager;
import example.com.cominghome.data.database.RouteTable;

public class Utils {
    public static final String SHARED_PREFERENCES_NAME = "prefs";

    public static final String BTN_GO_STATE_KEY = "1 enabled";
    public static final String BTN_GO_HOME_STATE_KEY = "2 enabled";
    public static final String ZOOM_KEY = "zoom";

    public static final String MAP_TYPE_KEY = "map type key";

    public static final String RECORD_MODE_KEY = "record mode key";

    //
    public static final int GROUP_MAP = 0;
    public static final int GROUP_MAP_VIEW = 1;
    public static final int GROUP_RESET = 2;
    public static final int GROUP_OPTIONS = 3;
    public static final int GROUP_ABOUT = 4;

    // options
    public static final String TRACK_MODE_KEY = "track mode key";
    public static final int TRACK_MODE_ON = 0;
    public static final int TRACK_MODE_OFF = 1;
    public static final int TRACK_MODE_ASK = 2;
    // that was selected exactly
    public static final String TRACK_MODE_RADIO_BUTTON_ID_KEY = "track mode radio button id key";
    public static final String TURNING_MODE_KEY = "turning mode key";
    public static final String ADDITIONAL_INFO_MODE_KEY = "add info mode key";


    private static RouteTable routeTable = DBManager.getHelper().getRouteTable();

    public static SharedPreferences getAppPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        return prefs;
    }

    public static String getAddress(Context context, LatLng coords) {
        String curAddress = "";
        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> adds = geocoder.getFromLocation(coords.latitude, coords.longitude, 1);
            if (adds != null && adds.size() > 0) {
                Address add = adds.get(0);
                int max = add.getMaxAddressLineIndex();
                if (max != -1) {
                    for (int i = 0; i < max; i++)
                        curAddress += add.getAddressLine(i) + " ";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curAddress;
    }

    // сюда надо будет попробовать впилить шаред префс и прочие дополнительные методы и выпилить из MapsFragment
    // SensorListener
    // OnMapReadyCallBackListener
    // LocationReceiver

}
