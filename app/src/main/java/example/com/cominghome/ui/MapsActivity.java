package example.com.cominghome.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import example.com.cominghome.R;
import example.com.cominghome.app.App;
import example.com.cominghome.background.LocationService;
import example.com.cominghome.data.DBManager;
import example.com.cominghome.data.RouteTable;


public class MapsActivity extends FragmentActivity {

    public static boolean isActivated;

    private GoogleMap mMap;

    private Marker beginLocation, endLocation, currentLocation;

    private RouteTable routeTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (mMap == null)
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        routeTable = DBManager.getHelper().getRouteTable();

        setButtons();

        setOnCurrentMarkerRotation();
    }

    private void setOnCurrentMarkerRotation() {
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
        //final Button btnTurnMode = (Button) findViewById(R.id.btn_turn_mode);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (App.getMe() != null)
                        beginLocation = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(App.getMe().getLatitude(), App.getMe().getLongitude()))
                                .title("start")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    else
                        Log.d(App.TAG, "MapsActivity.Go: can't set first marker");
                    LocationService.startRecord();
                    btnGo.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btnGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocationService.stopRecord();

                    LatLng latlngLastLocation = new LatLng(
                            Double.parseDouble(routeTable.getLastLocation().getLatitude()),
                            Double.parseDouble(routeTable.getLastLocation().getLongtitude()));
                    endLocation = mMap.addMarker(new MarkerOptions().position(latlngLastLocation).title("end"));

                    LatLng latLngCurrentLoc = new LatLng(App.getMe().getLatitude(), App.getMe().getLongitude());
                    currentLocation = mMap.addMarker(new MarkerOptions()
                            .position(latLngCurrentLoc)
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
                    LocationService.stopRecord();

                    beginLocation.remove();
                    endLocation.remove();
                    currentLocation.remove();

                    beginLocation = null;
                    endLocation = null;
                    currentLocation = null;

                    routeTable.deleteRoute();

                    btnGo.setEnabled(true);
                    btnGoHome.setEnabled(true);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
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
        isActivated = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivated = false;
    }
}
