package example.com.cominghome.data;

import android.content.Context;

public class DBManager {
    private static DBHelper helper;

    public static void initHelper(Context context) {
        if (helper == null)
            helper = new DBHelper(context);
    }

    public static void closeHelper() {
        if (helper != null) {
            helper.close();
            helper = null;
        }
    }

    public static DBHelper getHelper() {
        return helper;
    }
}
