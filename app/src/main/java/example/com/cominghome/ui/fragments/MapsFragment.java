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
import android.widget.TextView;
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
import example.com.cominghome.data.database.DBManager;
import example.com.cominghome.data.database.RouteTable;
import example.com.cominghome.utils.MapRotateSensorListener;
import example.com.cominghome.utils.MarkerRotateSensorListener;
import example.com.cominghome.utils.Utils;

import static example.com.cominghome.app.App.TAG;
import static example.com.cominghome.utils.Utils.ADDITIONAL_INFO_MODE_KEY;
import static example.com.cominghome.utils.Utils.BTN_GO_HOME_STATE_KEY;
import static example.com.cominghome.utils.Utils.BTN_GO_STATE_KEY;
import static example.com.cominghome.utils.Utils.TURNING_MODE_KEY;
import static example.com.cominghome.utils.Utils.ZOOM_KEY;
import static example.com.cominghome.utils.Utils.getAddress;
import static example.com.cominghome.utils.Utils.getAppPreferences;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;
    private Button btnGo;
    private Button btnGoHome;

    private Marker beginLocation, currentLocation, endLocation;
    private TextView tvPointA, tvPointB, tvPointMe;

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

        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        tvPointA = (TextView) rootView.findViewById(R.id.tv_area_point_a_value);
        tvPointB = (TextView) rootView.findViewById(R.id.tv_area_point_b_value);
        tvPointMe = (TextView) rootView.findViewById(R.id.tv_area_point_me_value);

        manager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);


        final SupportMapFragment supportMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map));
        if (mMap == null)
            supportMapFragment.getMapAsync(new OnMapReadyCallBackListener());

        isTurningMode = getAppPreferences(getActivity()).getBoolean(TURNING_MODE_KEY, false);


        return rootView;
    }

    private void setCurrentLocationZoomTMode() {
        try {
            LatLng latLngCurrent = new LatLng(
                    App.getApp(getActivity()).getMe().getLatitude(),
                    App.getApp(getActivity()).getMe().getLongitude());
            MarkerOptions options = new MarkerOptions().position(latLngCurrent);
            float zoom = getAppPreferences(getActivity()).getFloat(ZOOM_KEY, 2.0f);

            if (!routeTable.isEmpty()) {
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker());
            }

            currentLocation = mMap.addMarker(options);
            tvPointMe.setText(latLngCurrent.toString());
            markerListener = new MarkerRotateSensorListener(currentLocation);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation.getPosition(), zoom));
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
            //mMap.setMyLocationEnabled(true);
            if (getArguments() != null) {
                int type = getArguments().getInt(Utils.MAP_TYPE_KEY);
                if (mMap.getMapType() != type)
                    mMap.setMapType(type);
            }

            createButtons();
            setButtonsState();

            receiver = new LocationReceiver();
            getActivity().registerReceiver(receiver, receiver.getBroadcastFilter());

            setCurrentLocationZoomTMode();

            showAdditionalInfo();
        }
    }

    private void createButtons() {
        Log.d(TAG, "createButtons");
        btnGo = (Button) getActivity().findViewById(R.id.btn_go);
        btnGoHome = (Button) getActivity().findViewById(R.id.btn_go_home);
        Button btnReset = (Button) getActivity().findViewById(R.id.btn_reset);

        //region go
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getAppPreferences(getActivity())
                            .edit()
                            .putFloat(ZOOM_KEY, mMap.getCameraPosition().zoom)
                            .commit();
                    getActivity().startService(new Intent(LocationService.ACTION_START_RECORD));
                    getAppPreferences(getActivity())
                            .edit()
                            .putBoolean(BTN_GO_STATE_KEY, false)
                            .commit();
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
                    //getAppPreferences(getActivity()).edit().putBoolean(BTN_GO_STATE_KEY, true).commit();
                    getAppPreferences(getActivity()).edit().putBoolean(BTN_GO_HOME_STATE_KEY, false).commit();
                    tvPointB.setText(getAddress(getActivity(), latlngLast) + "\n" + latlngLast.toString());

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

                    tvPointA.setText(getString(R.string.area_point_a_value));
                    tvPointB.setText(getString(R.string.area_point_b_value));

                    getAppPreferences(getActivity())
                            .edit()
                            .putBoolean(TURNING_MODE_KEY, false)
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

    private void setButtonsState() {
        Log.d(TAG, "setButtonsState");
        boolean isGoEnabled, isGoHomeEnabled;
        isGoEnabled = getAppPreferences(getActivity()).getBoolean(BTN_GO_STATE_KEY, true);
        isGoHomeEnabled = getAppPreferences(getActivity()).getBoolean(BTN_GO_HOME_STATE_KEY, true);

        try {
            if (!isGoEnabled) {
                LatLng latlngBegin = new LatLng(
                        Double.parseDouble(routeTable.getFirstLocation().getLatitude()),
                        Double.parseDouble(routeTable.getFirstLocation().getLongtitude()));
                beginLocation = mMap.addMarker(new MarkerOptions().position(latlngBegin).title("start")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                btnGo.setEnabled(false);
                tvPointA.setText(getAddress(getActivity(), latlngBegin) + "\n" + latlngBegin.toString());
            }
            if (!isGoHomeEnabled) {
                LatLng latlngEnd = new LatLng(
                        Double.parseDouble(routeTable.getLastLocation().getLatitude()),
                        Double.parseDouble(routeTable.getLastLocation().getLongtitude()));
                endLocation = mMap.addMarker(new MarkerOptions().position(latlngEnd).title("end"));
                btnGoHome.setEnabled(false);
                tvPointB.setText(getAddress(getActivity(), latlngEnd) + "\n" + latlngEnd.toString());
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //Log.d(App.TAG, "data was loaded: 1 enabled - " + isGoEnabled + ", 2 enabled - " + isGoHomeEnabled);
    }

    private boolean showAdditionalInfo() {
        boolean mode = getAppPreferences(getActivity()).getBoolean(ADDITIONAL_INFO_MODE_KEY, true);

        if (mode) {
            getActivity().findViewById(R.id.area_additional_info).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.area_additional_info).setVisibility(View.GONE);
        }

        return mode;
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

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume");
//
//
//        //setCurrentLocationZoomTMode();
//
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.d(TAG, "onStart");
//        //setButtonsState();
//    }

    @Override
    public void onStop() {
        saveMapState();
        super.onStop();
        Log.d(TAG, "onStop");
//        saveMapState();
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
                tvPointA.setText(getAddress(getActivity(), beginLocation.getPosition()) + "\n"
                        + beginLocation.getPosition().toString());

                if (currentLocation != null)
                    currentLocation.remove();
                currentLocation = null;
                setCurrentLocationZoomTMode();
            }
        }
    }
}
