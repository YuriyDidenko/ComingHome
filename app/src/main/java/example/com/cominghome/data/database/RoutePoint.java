package example.com.cominghome.data.database;

public class RoutePoint {
    public static final String FIELD_ID = "_id";
    public static final String FIELD_LATITUDE = "latitude";
    public static final String FIELD_LONGTITUDE = "longtitude";

    private String id;
    private String latitude;
    private String longtitude;

    public RoutePoint(String id, String latitude, String longtitude) {
        this.id = id;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    @Override
    public boolean equals(Object o) {
        // if (o instanceof RoutePoint)
        RoutePoint point = (RoutePoint) o;
        return id.equals(point.getId()) &&
                latitude.equals(point.getLatitude()) &&
                longtitude.equals(point.getLongtitude());
    }

    @Override
    public String toString() {
        return id + ": " + latitude + ", " + longtitude;
    }
}
