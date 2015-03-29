package example.com.cominghome.data;

public class RoutePoint {
    public static final String FIELD_ID = "_id";
    public static final String FIELD_LATITUDE = "latitude";
    public static final String FIELD_LONGTITUDE = "longtitude";
    //public static final String FIELD_TIME = "time";

    private String id;
    private String latitude;
    private String longtitude;
    //private String time;

    public RoutePoint(String id, String latitude, String longtitude/*, String time*/) {
        this.id = id;
        this.latitude = latitude;
        this.longtitude = longtitude;
        //this.time = time;
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

    /*public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }*/
}
