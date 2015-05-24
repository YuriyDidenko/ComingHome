package example.com.cominghome.ui.fragments;

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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import example.com.cominghome.utils.Utils;

import static example.com.cominghome.utils.Utils.BTN_GO_HOME_STATE_KEY;
import static example.com.cominghome.utils.Utils.BTN_GO_STATE_KEY;
import static example.com.cominghome.utils.Utils.SHARED_PREFERENCES_NAME;
import static example.com.cominghome.utils.Utils.ZOOM_KEY;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;
    private Button btnGo;
    private Button btnGoHome;

    private Marker beginLocation, currentLocation, endLocation;

    private RouteTable routeTable;
    private LocationReceiver receiver;
    private SensorManager manager;
    private SensorListener listener = new SensorListener();

    public MapsFragment() {
        Log.d(App.TAG, "new MapsFrament()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        manager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        receiver = new LocationReceiver();
        getActivity().registerReceiver(receiver, receiver.getBroadcastFilter());

        final SupportMapFragment supportMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map));
        if (mMap == null)
            supportMapFragment.getMapAsync(new OnMapReadyCallBackListener());


        routeTable = DBManager.getHelper().getRouteTable();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setCurrentLocationMarker() {
        try {
            LatLng latLngCurrent = new LatLng(
                    App.getApp(getActivity()).getMe().getLatitude(),
                    App.getApp(getActivity()).getMe().getLongitude());

            MarkerOptions options = new MarkerOptions().position(latLngCurrent);

            if (routeTable.getFirstLocation() != null) {
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow));
                manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker());
                manager.unregisterListener(listener);
            }

            currentLocation = mMap.addMarker(options);
            currentLocation.setRotation(0);

        } catch (Exception e) {
            Toast.makeText(App.getApp(getActivity()),
                    "check your connection or availability of GPS-module", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private class OnMapReadyCallBackListener implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            setButtons();
            loadMapState();

            if (getArguments() != null) {
                int type = getArguments().getInt(Utils.MAP_TYPE_KEY);
                if (mMap.getMapType() != type)
                    mMap.setMapType(type);
            }

            setCurrentLocationMarker();
        }
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (currentLocation != null)
                currentLocation.setRotation(event.values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private void setButtons() {
        btnGo = (Button) getActivity().findViewById(R.id.btn_go);
        btnGoHome = (Button) getActivity().findViewById(R.id.btn_go_home);
        Button btnReset = (Button) getActivity().findViewById(R.id.btn_reset);

        //region go
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getActivity().startService(new Intent(LocationService.ACTION_START_RECORD));
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

                    getActivity().startService(new Intent(LocationService.ACTION_STOP_RECORD));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
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
                    getActivity().startService(new Intent(LocationService.ACTION_STOP_RECORD));

                    removeSavedData();

                    // тут на выбор, оставлять или нет маркер, указывающий, где ты был последний раз
                    //setCurrentLocationMarker();

                    btnGo.setEnabled(true);
                    btnGoHome.setEnabled(true);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        Button btn = (Button) getActivity().findViewById(R.id.btn_turn_mode);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        //endregion
    }

    //region shared_prefs
    private void saveMapState() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(BTN_GO_STATE_KEY, btnGo.isEnabled());
        editor.putBoolean(BTN_GO_HOME_STATE_KEY, btnGoHome.isEnabled());
        editor.putFloat(ZOOM_KEY, mMap.getCameraPosition().zoom);
        editor.apply();

        //Log.d(App.TAG, "data was saved: 1 enabled - " + btnGo.isEnabled() + ", 2 enabled - " + btnGoHome.isEnabled());
    }

    private void loadMapState() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        boolean isGoEnabled, isGoHomeEnabled;
        isGoEnabled = prefs.getBoolean(BTN_GO_STATE_KEY, true);
        isGoHomeEnabled = prefs.getBoolean(BTN_GO_HOME_STATE_KEY, true);
        float zoom = prefs.getFloat(ZOOM_KEY, 2.0f);

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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        App.getApp(getActivity()).getMe().getLatitude(),
                        App.getApp(getActivity()).getMe().getLongitude()),
                zoom));

        //Log.d(App.TAG, "data was loaded: 1 enabled - " + isGoEnabled + ", 2 enabled - " + isGoHomeEnabled);
    }

    private void removeSavedData() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(BTN_GO_STATE_KEY);
        editor.remove(BTN_GO_HOME_STATE_KEY);
        editor.remove(ZOOM_KEY);
        editor.clear();
        editor.apply();

        if (beginLocation != null) {
            beginLocation.remove();
            beginLocation = null;
        }
        if (endLocation != null) {
            endLocation.remove();
            endLocation = null;
        }

        routeTable.deleteRoute();

        if (currentLocation != null) {
            currentLocation.remove();
//            currentLocation = null;
        }
    }
    //endregion

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        setCurrentLocationMarker();
    }

    @Override
    public void onStart() {
        super.onStart();
//        loadMapState();
    }

    @Override
    public void onStop() {
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

                if (currentLocation != null)
                    currentLocation.remove();
                currentLocation = null;
                setCurrentLocationMarker();
            }
        }
    }
}
