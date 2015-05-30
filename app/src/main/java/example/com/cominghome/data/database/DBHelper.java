package example.com.cominghome.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database";

    private RouteTable routeTable;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        routeTable = new RouteTable(getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        RouteTable.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        RouteTable.drop(db);
        onCreate(db);
    }

    public RouteTable getRouteTable() {
        return routeTable;
    }
}
