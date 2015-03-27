package example.com.cominghome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;


public class MapsActivity extends FragmentActivity {

    private static final String MY_ROUTE_POINTS_KEY = "key";
    public static final String TAG = "myTag";

    private SharedPreferences preferences;
    private LocationManager mLocationManager;

    private GoogleMap mMap;
    private LinkedList<LatLng> routeList = new LinkedList<>();

    private LocationListener mLocationListener;
    private Location me;
    private Marker beginLocation, endLocation, currentLocation;

    private float mDeclination;
    private float[] mRotationMatrix = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (mMap == null)
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        //clearMyRoute();
        routeList = parseRouteByString(getMyRouteString());
        
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (me != null)
                    if (location.getLatitude() == me.getLatitude() && location.getLongitude() == me.getLongitude())
                        return;
                me = location;
                Log.d(TAG, "Latitude " + location.getLatitude() + ", longitude " + location.getLongitude());
                //routeList.add(new LatLng(location.getLatitude(), location.getLongitude()));
                suka();
                if (null != currentLocation)
                    currentLocation.setPosition(new LatLng(me.getLatitude(), me.getLongitude()));
                GeomagneticField field = new GeomagneticField(
                        (float) location.getLatitude(),
                        (float) location.getLongitude(),
                        (float) location.getAltitude(),
                        System.currentTimeMillis()
                );
                mDeclination = field.getDeclination();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled");
            }
        };

//        me = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        Log.d(TAG, "Resume: routelist.size =  " + routeList.size());

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
//                if (routeList.isEmpty())
//                    suka();
                if (routeList.size() > 1)
                    drawLine();
            }
        });
        //sensorListenerOn();

        setButtons();

        set2();

    }

    private void sensorListenerOn() {
        SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensorRotation = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        SensorEventListener rotationEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(mRotationMatrix, orientation);
                    double bearing = Math.toDegrees(orientation[0] + mDeclination);
                    //updateCamera(bearing);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        manager.registerListener(rotationEventListener, sensorRotation, SensorManager.SENSOR_DELAY_GAME);
    }

    private void updateCamera(double bearing) {
        CameraPosition oldPos = mMap.getCameraPosition();
        CameraPosition newPos = CameraPosition.builder(oldPos).
                bearing((float) bearing).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPos));
    }

    private void set2() {
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        float orient = event.values[0];
                        if (currentLocation != null)
                            currentLocation.setRotation(orient);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                }, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setButtons() {
        final Button btnGo = (Button) findViewById(R.id.btn_go);
        final Button btnGoHome = (Button) findViewById(R.id.btn_go_home);
        final Button btnReset = (Button) findViewById(R.id.btn_reset);
        final Button btnTurnMode = (Button) findViewById(R.id.btn_turn_mode);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Haloooo", Toast.LENGTH_SHORT).show();
                try {
                    beginLocation = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(me.getLatitude(), me.getLongitude())).title("start")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    btnGo.setEnabled(false);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        btnGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "go home", Toast.LENGTH_SHORT).show();
                try {
                    endLocation = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(me.getLatitude(), me.getLongitude())).title("end"));
                    currentLocation = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(me.getLatitude(), me.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
                    btnGoHome.setEnabled(false);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "reset", Toast.LENGTH_SHORT).show();
                try {
                    beginLocation.remove();
                    endLocation.remove();
                    currentLocation.remove();
                    beginLocation = null;
                    endLocation = null;
                    btnGo.setEnabled(true);
                    btnGoHome.setEnabled(true);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        btnTurnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        //mLocationManager.removeUpdates(mLocationListener);
        saveMyRoute();
        mLocationManager.removeUpdates(mLocationListener);
        Log.d(TAG, "Pause: routelist.size =  " + routeList.size());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void suka() {
        //me = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            Toast.makeText(this, "Провайдер есть", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "нету провайдера даже", Toast.LENGTH_SHORT).show();

//        if (routeList.size() == 0)
//            Toast.makeText(this, "не существует getLastKnowLocation", Toast.LENGTH_SHORT).show();
//        else {
        LatLng latLng = new LatLng(me.getLatitude(), me.getLongitude());
        Toast.makeText(this, "lat: " + latLng.latitude + "\nlong: " + latLng.longitude,
                Toast.LENGTH_SHORT).show();
//            if (!routeList.getLast().equals(latLng))
        routeList.add(latLng);
//        }
        Log.d(TAG, "suka: routelist.size = " + routeList.size());
    }

    private void drawLine() {
        Polyline line;
        PolylineOptions options = new PolylineOptions();
        LatLng[] mas = routeList.toArray(new LatLng[routeList.size()]);
        options.add(mas)
                .width(5)
                .color(Color.GREEN);
        line = mMap.addPolyline(options);
    }
//          private String getAddress(LatLng coords) {
//        String curAddress = "";
//        try {
//            Geocoder geocoder = new Geocoder(this);
//            List<Address> adds = geocoder.getFromLocation(coords.latitude, coords.longitude, 1);
//            if (adds != null && adds.size() > 0) {
//                Address add = adds.get(0);
//                int max = add.getMaxAddressLineIndex();
//                if (max != -1) {
//                    for (int i = 0; i < max; i++)
//                        curAddress += add.getAddressLine(i) + " ";
//                }
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "fuck you", Toast.LENGTH_SHORT).show();
//        }
//        return curAddress;
//    }
//
//    private LatLng getAddress(String strAddress) {
//        Geocoder coder = new Geocoder(this);
//        List<Address> address;
//        try {
//            address = coder.getFromLocationName(strAddress, 5);
//            if (address == null) {
//                return null;
//            }
//            Address location = address.get(0);
//
//            return new LatLng(location.getLatitude(), location.getLongitude());
//        } catch (IOException e) { e.printStackTrace(); }
//        return new LatLng(0, 0);
//    }

    private void saveMyRoute() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MY_ROUTE_POINTS_KEY, routeList.toString());
        editor.commit();
    }

    private String getMyRouteString() {
        preferences = getPreferences(MODE_PRIVATE);
        String routeString = preferences.getString(MY_ROUTE_POINTS_KEY, "route is empty");
        return routeString;
    }

    private void clearMyRoute() {
        routeList.clear();
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MY_ROUTE_POINTS_KEY, "");
        editor.commit();
    }

    private LinkedList<LatLng> parseRouteByString(String routeString) {
        LinkedList<LatLng> result = new LinkedList<>();

        String s = routeString.replace("[", "").replace("]", "").replace("lat/lng:", "").replace(" ", "").replace("(", "").replace(")", "");
        String[] couples = s.split(",");
        LatLng latLng;
        for (int i = 0; i < couples.length - 1; i += 2) {
            latLng = new LatLng(Double.parseDouble(couples[i]), Double.parseDouble(couples[i + 1]));
            result.add(latLng);
        }
        return result;
    }
}
