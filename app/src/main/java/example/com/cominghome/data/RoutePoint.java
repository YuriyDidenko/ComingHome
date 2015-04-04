package example.com.cominghome.data;

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

    public void setId(String id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    @Override
    public boolean equals(Object o) {
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
