package example.com.cominghome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;


public class MapsActivity extends FragmentActivity {

    private static final String MY_ROUTE_POINTS_KEY = "key";
    public static final String TAG = "myTag";
    public static boolean active;

    private SharedPreferences preferences;

    private GoogleMap mMap;
    private LinkedList<LatLng> routeList = new LinkedList<>();

    private Location me;
    private Marker beginLocation, endLocation, currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (mMap == null)
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        routeList = parseRouteByString(getMyRouteString());

        Log.d(TAG, "Resume: routelist.size =  " + routeList.size());

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (routeList.size() > 1)
                    drawLine();
            }
        });
        setButtons();

        setOnGoHomeMarkerListener();
    }

    private void setOnGoHomeMarkerListener() {
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
        saveMyRoute();
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

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
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
