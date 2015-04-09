package example.com.cominghome.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import example.com.cominghome.R;
import example.com.cominghome.app.App;
import example.com.cominghome.background.LocationService;
import example.com.cominghome.data.DBManager;
import example.com.cominghome.data.RouteTable;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private Button btnGo;
    private Button btnGoHome;

    private Marker beginLocation, endLocation;

    private RouteTable routeTable;
    private LocationReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        receiver = new LocationReceiver();
        registerReceiver(receiver, receiver.getBroadcastFilter());

        if (mMap == null)
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        routeTable = DBManager.getHelper().getRouteTable();

        setButtons();

    }

    private void setCurrentLocationMarker() {
        LatLng latLngCurrent = new LatLng(
                App.getApp(MapsActivity.this).getMe().getLatitude(),
                App.getApp(MapsActivity.this).getMe().getLongitude());
        final Marker currentLocation = mMap.addMarker(new MarkerOptions().position(latLngCurrent)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
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
        btnGo = (Button) findViewById(R.id.btn_go);
        btnGoHome = (Button) findViewById(R.id.btn_go_home);
        Button btnReset = (Button) findViewById(R.id.btn_reset);

        //region go
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(App.TAG, "Go: before startservice");

                    startService(new Intent(LocationService.ACTION_START_RECORD));

                    Log.d(App.TAG, "Go: after startservice");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //endregion

        //region home
        btnGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LatLng latlngLast = new LatLng(
                            Double.parseDouble(routeTable.getLastLocation().getLatitude()),
                            Double.parseDouble(routeTable.getLastLocation().getLongtitude()));
                    endLocation = mMap.addMarker(new MarkerOptions().position(latlngLast).title("end"));
                    btnGoHome.setEnabled(false);

                    startService(new Intent(LocationService.ACTION_STOP_RECORD));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        //endregion

        //region reset
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startService(new Intent(LocationService.ACTION_STOP_RECORD));

                    if (beginLocation != null) {
                        beginLocation.remove();
                        beginLocation = null;
                    }
                    if (endLocation != null) {
                        endLocation.remove();
                        endLocation = null;
                    }

                    routeTable.deleteRoute();

                    btnGo.setEnabled(true);
                    btnGoHome.setEnabled(true);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        //endregion
    }

    //region shared_prefs
    private void saveMapState() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("1 enabled", btnGo.isEnabled());
        editor.putBoolean("2 enabled", btnGoHome.isEnabled());
        editor.commit();

        //Log.d(App.TAG, "data was saved: 1 enabled - " + btnGo.isEnabled() + ", 2 enabled - " + btnGoHome.isEnabled());
    }

    private void loadMapState() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isGoEnabled, isGoHomeEnabled;
        isGoEnabled = prefs.getBoolean("1 enabled", true);
        isGoHomeEnabled = prefs.getBoolean("2 enabled", true);

        if (!isGoEnabled) {
            LatLng latlngBegin = new LatLng(
                    Double.parseDouble(routeTable.getFirstLocation().getLatitude()),
                    Double.parseDouble(routeTable.getFirstLocation().getLongtitude()));
            beginLocation = mMap.addMarker(new MarkerOptions().position(latlngBegin).title("start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            btnGo.setEnabled(false);
        }
        if (!isGoHomeEnabled) {
            LatLng latlngEnd = new LatLng(
                    Double.parseDouble(routeTable.getLastLocation().getLatitude()),
                    Double.parseDouble(routeTable.getLastLocation().getLongtitude()));
            endLocation = mMap.addMarker(new MarkerOptions().position(latlngEnd).title("end"));
            btnGoHome.setEnabled(false);
        }

        //Log.d(App.TAG, "data was loaded: 1 enabled - " + isGoEnabled + ", 2 enabled - " + isGoHomeEnabled);
    }

    private void removeSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("1 enabled");
        editor.remove("2 enabled");
        editor.clear();
        editor.commit();
    }
    //endregion

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurrentLocationMarker();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMapState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveMapState();
    }

    private class LocationReceiver extends BroadcastReceiver {

        public IntentFilter getBroadcastFilter() {
            return new IntentFilter(LocationService.ACTION_SEND_FIRST_POINT);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationService.ACTION_SEND_FIRST_POINT.equals(intent.getAction())) {

                Location firstLoc = intent.getParcelableExtra(LocationService.EXTRA_FIRST_POINT);

                beginLocation = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(firstLoc.getLatitude(), firstLoc.getLongitude()))
                        .title("start")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                btnGo.setEnabled(false);
            }
        }
    }
}
