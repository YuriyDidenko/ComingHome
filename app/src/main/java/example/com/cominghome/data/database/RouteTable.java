package example.com.cominghome.data.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
        mDatabase.insert(TABLE_NAME, null, cv);

        Log.d(App.TAG, "added new point to db");

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
            do {
                String id = c.getString(indexId);
                String latitude = c.getString(indexLatitude);
                String longitude = c.getString(indexLongitude);
                RoutePoint point = new RoutePoint(id, latitude, longitude);
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

            String id = c.getString(indexId);
            String latitude = c.getString(indexLatitude);
            String longitude = c.getString(indexLongitude);

            point = new RoutePoint(id, latitude, longitude);
        } else Log.d(App.TAG, "cannot find the last location from table");
        c.close();

        return point;
    }

    public RoutePoint getFirstLocation() {
        RoutePoint point = null;
        Cursor c = mDatabase.rawQuery("select * from " + TABLE_NAME +
                " order by " + RoutePoint.FIELD_ID + " asc " +
                "limit 1", null);
        if (c.moveToFirst()) {
            int indexId = c.getColumnIndex(RoutePoint.FIELD_ID);
            int indexLatitude = c.getColumnIndex(RoutePoint.FIELD_LATITUDE);
            int indexLongitude = c.getColumnIndex(RoutePoint.FIELD_LONGTITUDE);

            String id = c.getString(indexId);
            String latitude = c.getString(indexLatitude);
            String longitude = c.getString(indexLongitude);

            point = new RoutePoint(id, latitude, longitude);
        } else Log.d(App.TAG, "cannot find the first location from table");
        c.close();

        return point;
    }

    public boolean isEmpty() {
        return getFirstLocation() == null;
    }

    public boolean contains(RoutePoint point) {
        return getFullRoute().contains(point);
    }

    public boolean contains(Location location) {
        for (RoutePoint p : getFullRoute())
            if (location.getLatitude() == Double.parseDouble(p.getLatitude())
                    && location.getLongitude() == Double.parseDouble(p.getLongtitude()))
                return true;
        return false;
    }

    public boolean contains(LatLng latLng) {
        for (RoutePoint p : getFullRoute())
            if (latLng.latitude == Double.parseDouble(p.getLatitude())
                    && latLng.longitude == Double.parseDouble(p.getLongtitude()))
                return true;
        return false;
    }

    @Override
    public String toString() {
        Log.d(App.TAG, "RouteTable.toString(): override me!");
        return null;
    }
}
