package example.com.cominghome.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
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
import example.com.cominghome.utils.MapRotateSensorListener;
import example.com.cominghome.utils.MarkerRotateSensorListener;
import example.com.cominghome.utils.Utils;

import static example.com.cominghome.app.App.TAG;
import static example.com.cominghome.utils.Utils.BTN_GO_HOME_STATE_KEY;
import static example.com.cominghome.utils.Utils.BTN_GO_STATE_KEY;
import static example.com.cominghome.utils.Utils.TURNING_MODE_KEY;
import static example.com.cominghome.utils.Utils.ZOOM_KEY;
import static example.com.cominghome.utils.Utils.getAppPreferences;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;
    private Button btnGo;
    private Button btnGoHome;

    private Marker beginLocation, currentLocation, endLocation;

    private RouteTable routeTable;
    private LocationReceiver receiver;
    private SensorManager manager;
    private MarkerRotateSensorListener markerListener;
    private MapRotateSensorListener mapListener;

    private boolean isTurningMode;

    public MapsFragment() {
        Log.d(TAG, "new MapsFragment()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        routeTable = DBManager.getHelper().getRouteTable();

        Log.d(TAG, "onCreateView, route table is empty?" + routeTable.isEmpty());

        //routeTable = DBManager.getHelper().getRouteTable();
        //removeSavedData();
        //Log.d(TAG, "after removeSavedData, route table is empty?" + routeTable.isEmpty());

        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        manager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        final SupportMapFragment supportMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map));
        if (mMap == null)
            supportMapFragment.getMapAsync(new OnMapReadyCallBackListener());

        isTurningMode = getAppPreferences(getActivity()).getBoolean(TURNING_MODE_KEY, false);


        return rootView;
    }

    private void setCurrentLocationMarker() {
        Log.d(TAG, "setCurrentLocationMarker");
        try {
            LatLng latLngCurrent = new LatLng(
                    App.getApp(getActivity()).getMe().getLatitude(),
                    App.getApp(getActivity()).getMe().getLongitude());

            MarkerOptions options = new MarkerOptions().position(latLngCurrent);


            if (!routeTable.isEmpty()) {
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker());
            }

            currentLocation = mMap.addMarker(options);
            markerListener = new MarkerRotateSensorListener(currentLocation);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation.getPosition()));
            mapListener = new MapRotateSensorListener(mMap);

            if (isTurningMode) {
                if (!routeTable.isEmpty())
                    manager.registerListener(mapListener, manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                            SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                if (!routeTable.isEmpty()) {
                    manager.unregisterListener(mapListener);
                    manager.registerListener(markerListener, manager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                            SensorManager.SENSOR_DELAY_NORMAL);
                } else
                    manager.unregisterListener(markerListener);
            }

            currentLocation.setRotation(0);
        } catch (Exception e) {
            Toast.makeText(App.getApp(getActivity()),
                    "check your connection or availability of GPS-module", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private class OnMapReadyCallBackListener implements OnMapReadyCallback {
        public OnMapReadyCallBackListener() {
            Log.d(TAG, "onMapReadyCallBackListener");
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.d(TAG, "onMapReady");
            mMap = googleMap;
            setButtons();
            loadMapState();

            if (getArguments() != null) {
                int type = getArguments().getInt(Utils.MAP_TYPE_KEY);
                if (mMap.getMapType() != type)
                    mMap.setMapType(type);
            }
            receiver = new LocationReceiver();
            getActivity().registerReceiver(receiver, receiver.getBroadcastFilter());

            setCurrentLocationMarker();

        }
    }

    private void setButtons() {
        Log.d(TAG, "setButtons");
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

                    btnGo.setEnabled(true);
                    btnGoHome.setEnabled(true);

                    getAppPreferences(getActivity())
                            .edit().
                            putBoolean(TURNING_MODE_KEY, false)
                            .commit();

                    manager.unregisterListener(mapListener);

                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        //endregion
    }

    //region shared_prefs
    private void saveMapState() {
        SharedPreferences.Editor editor = getAppPreferences(getActivity()).edit();

        //editor.putBoolean(BTN_GO_STATE_KEY, btnGo.isEnabled());
        editor.putBoolean(BTN_GO_HOME_STATE_KEY, btnGoHome.isEnabled());
        editor.putFloat(ZOOM_KEY, mMap.getCameraPosition().zoom);
        editor.apply();

        //Log.d(App.TAG, "data was saved: 1 enabled - " + btnGo.isEnabled() + ", 2 enabled - " + btnGoHome.isEnabled());
    }

    private void loadMapState() {
        Log.d(TAG, "loadMapState");
        boolean isGoEnabled, isGoHomeEnabled;
        isGoEnabled = getAppPreferences(getActivity()).getBoolean(BTN_GO_STATE_KEY, true);
        isGoHomeEnabled = getAppPreferences(getActivity()).getBoolean(BTN_GO_HOME_STATE_KEY, true);
        float zoom = getAppPreferences(getActivity()).getFloat(ZOOM_KEY, 2.0f);

        try {
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //Log.d(App.TAG, "data was loaded: 1 enabled - " + isGoEnabled + ", 2 enabled - " + isGoHomeEnabled);
    }

    private void removeSavedData() {
        SharedPreferences.Editor editor = getAppPreferences(getActivity()).edit();
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

        if (routeTable != null)
            routeTable.deleteRoute();

        if (currentLocation != null) {
            currentLocation.remove();
//            currentLocation = null;
        }
    }
    //endregion

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");


        //setCurrentLocationMarker();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
//        loadMapState();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
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
