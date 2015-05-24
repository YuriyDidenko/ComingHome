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

    public static final String MAP_TYPE_KEY = "map type key";

    public static final int GROUP_MAP = 0;
    public static final int GROUP_MAP_VIEW = 1;
    public static final int GROUP_RESET = 2;
    public static final int GROUP_OPTIONS = 3;
    public static final int GROUP_ABOUT = 4;


    private static RouteTable routeTable = DBManager.getHelper().getRouteTable();

    // сюда надо будет попробовать впилить шаред префс и прочие дополнительные методы и выпилить из MapsFragment
    // SensorListener
    // OnMapReadyCallBackListener
    // LocationReceiver

}
