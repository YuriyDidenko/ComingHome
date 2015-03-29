package example.com.cominghome.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import example.com.cominghome.app.App;

public class RouteTable {
    private static final String TABLE_NAME = "route";

    private SQLiteDatabase mDatabase;

    public RouteTable(SQLiteDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    public static void create(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME + "(" +
                RoutePoint.FIELD_ID + " text, " +
                RoutePoint.FIELD_LATITUDE + " text, " +
                RoutePoint.FIELD_LONGTITUDE + " text);";
        //RoutePoint.FIELD_TIME + " text);";
        db.execSQL(sql);
    }

    public static void drop(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_NAME);
    }

    public boolean addRoutePoint(RoutePoint point) {
        if (point == null)
            return false;
        ContentValues cv = new ContentValues();
        cv.put(RoutePoint.FIELD_ID, point.getId());
        cv.put(RoutePoint.FIELD_LATITUDE, point.getLatitude());
        cv.put(RoutePoint.FIELD_LONGTITUDE, point.getLongtitude());
        //cv.put(RoutePoint.FIELD_TIME, point.getTime());
        mDatabase.insert(TABLE_NAME, null, cv);

        return true;
    }

    public int deleteRoute() {
        return mDatabase.delete(TABLE_NAME, null, null);
    }

    public List<RoutePoint> getFullRoute() {
        List<RoutePoint> list = new ArrayList<>();
        Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int indexId = c.getColumnIndex(RoutePoint.FIELD_ID);
            int indexLatitude = c.getColumnIndex(RoutePoint.FIELD_LATITUDE);
            int indexLongitude = c.getColumnIndex(RoutePoint.FIELD_LONGTITUDE);
            //int indexTime = c.getColumnIndex(RoutePoint.FIELD_TIME);
            do {
                String id = c.getString(indexId);
                String latitude = c.getString(indexLatitude);
                String longitude = c.getString(indexLongitude);
                //String time = c.getString(indexTime);
                RoutePoint point = new RoutePoint(id, latitude, longitude/*, time*/);
                list.add(point);
            }
            while (c.moveToNext());
        } else
            Log.d(App.TAG, "table is empty");
        c.close();

        return list;
    }

    public RoutePoint getLastLocation() {
        RoutePoint point = null;
        Cursor c = mDatabase.rawQuery("select * from " + TABLE_NAME +
                " order by " + RoutePoint.FIELD_ID + " desc " +
                " limit 1", null);
        if (c.moveToFirst()) {
            int indexId = c.getColumnIndex(RoutePoint.FIELD_ID);
            int indexLatitude = c.getColumnIndex(RoutePoint.FIELD_LATITUDE);
            int indexLongitude = c.getColumnIndex(RoutePoint.FIELD_LONGTITUDE);
            //int indexTime = c.getColumnIndex(RoutePoint.FIELD_TIME);

            String id = c.getString(indexId);
            String latitude = c.getString(indexLatitude);
            String longitude = c.getString(indexLongitude);
            //String time = c.getString(indexTime);

            point = new RoutePoint(id, latitude, longitude/*, time*/);
        } else Log.d(App.TAG, "cannot find the last location from table");
        c.close();

        return point;
    }
}
