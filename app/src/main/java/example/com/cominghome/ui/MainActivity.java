package example.com.cominghome.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import example.com.cominghome.R;
import example.com.cominghome.background.LocationService;
import example.com.cominghome.ui.fragments.AboutFragment;
import example.com.cominghome.ui.fragments.AskDialogFragment;
import example.com.cominghome.ui.fragments.MapsFragment;
import example.com.cominghome.ui.fragments.OptionsFragment;
import example.com.cominghome.utils.DrawerExpItemAdapter;
import example.com.cominghome.utils.DrawerItem;
import example.com.cominghome.utils.Utils;

import static example.com.cominghome.utils.Utils.BTN_GO_STATE_KEY;
import static example.com.cominghome.utils.Utils.GROUP_ABOUT;
import static example.com.cominghome.utils.Utils.GROUP_MAP;
import static example.com.cominghome.utils.Utils.GROUP_MAP_VIEW;
import static example.com.cominghome.utils.Utils.GROUP_OPTIONS;
import static example.com.cominghome.utils.Utils.GROUP_RESET;
import static example.com.cominghome.utils.Utils.TRACK_MODE_ASK;
import static example.com.cominghome.utils.Utils.TRACK_MODE_KEY;
import static example.com.cominghome.utils.Utils.TRACK_MODE_OFF;
import static example.com.cominghome.utils.Utils.TRACK_MODE_ON;
import static example.com.cominghome.utils.Utils.getAppPreferences;


public class MainActivity extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerExpList;

    private Fragment currentFragment;
    private Bundle bundleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerExpList = (ExpandableListView) findViewById(R.id.left_drawer);

        DrawerItem[] drawerItems = new DrawerItem[5];

        drawerItems[GROUP_MAP] = new DrawerItem(R.drawable.ic_map_white_18dp, "Карта");
        drawerItems[GROUP_MAP_VIEW] = new DrawerItem(R.drawable.ic_more_vert_white_18dp, "Вид карты");
        drawerItems[GROUP_RESET] = new DrawerItem(R.drawable.ic_autorenew_white_18dp, "Reset");
        drawerItems[GROUP_OPTIONS] = new DrawerItem(R.drawable.ic_settings_white_18dp, "Options");
        drawerItems[GROUP_ABOUT] = new DrawerItem(R.drawable.ic_info_white_18dp, "About");

        DrawerExpItemAdapter adapter = new DrawerExpItemAdapter(this, drawerItems);
        mDrawerExpList.setAdapter(adapter);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, new Toolbar(this),
                R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerExpList.setOnGroupClickListener(new DrawerGroupClickListener());
        mDrawerExpList.setOnChildClickListener(new DrawerChildClickListener());

        bundleFragment = new Bundle();
        currentFragment = new MapsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();

    }


    private class DrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            switch (groupPosition) {
                case GROUP_MAP:
                    if (!(currentFragment instanceof MapsFragment))
                        currentFragment = new MapsFragment();
                    break;
                case GROUP_MAP_VIEW:
                    currentFragment = null;
                    break;
                case GROUP_RESET:
                    (findViewById(R.id.btn_reset)).performClick();
                    break;
                case GROUP_OPTIONS:
                    currentFragment = new OptionsFragment();
                    break;
                case GROUP_ABOUT:
                    currentFragment = new AboutFragment();
                    break;
            }

            if (currentFragment != null) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();

                mDrawerExpList.setItemChecked(groupPosition, true);
                mDrawerExpList.setSelection(groupPosition);
                if (groupPosition != GROUP_MAP_VIEW)
                    mDrawerLayout.closeDrawer(mDrawerExpList);
            }
            return false;
        }
    }

    private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {


            //if (groupPosition == MAP_TYPE_CHOICE)
            // ибо Normal уже = 1, а child минимальный = 0
            switch (childPosition + 1) {
                case GoogleMap.MAP_TYPE_NORMAL:
                    bundleFragment.putInt(Utils.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case GoogleMap.MAP_TYPE_SATELLITE:
                    bundleFragment.putInt(Utils.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case GoogleMap.MAP_TYPE_TERRAIN:
                    bundleFragment.putInt(Utils.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case GoogleMap.MAP_TYPE_HYBRID:
                    bundleFragment.putInt(Utils.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_HYBRID);
                    break;
                default:
                    bundleFragment.putInt(Utils.MAP_TYPE_KEY, -1);
                    break;
            }
            if (bundleFragment.getInt(Utils.MAP_TYPE_KEY) != -1) {
                currentFragment = new MapsFragment();
                currentFragment.setArguments(bundleFragment);

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
            }
            //mDrawerExpList.setItemChecked(childPosition, true);
            //mDrawerExpList.setSelection(childPosition);
            mDrawerLayout.closeDrawer(mDrawerExpList);

            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        startDialogIfNeeded();
    }

    // define if we need to show dialog
    private void startDialogIfNeeded() {
        switch (getAppPreferences(this).getInt(TRACK_MODE_KEY, TRACK_MODE_ON)) {
            case TRACK_MODE_ON:
                Toast.makeText(this, "track mode on", Toast.LENGTH_SHORT).show();
                //if (!App.isServiceRunning(this, LocationService.class))
//                    startService(new Intent(LocationService.ACTION_START_RECORD));
                // ничего не нужно запускать, ибо запущен и так
                if (LocationService.isRecordingMode())
                    getAppPreferences(this)
                            .edit()
                            .putBoolean(BTN_GO_STATE_KEY, false)
                            .commit();
                finish();
                break;
            case TRACK_MODE_OFF: // останавливаем запись, если запущен
                Toast.makeText(this, "track mode off", Toast.LENGTH_SHORT).show();
                if (LocationService.isRecordingMode()) {
                    startService(new Intent(LocationService.ACTION_STOP_RECORD));
                }
                finish();
                break;
            case TRACK_MODE_ASK: // не показываем диалог, если остановлена запись
                Toast.makeText(this, "track mode ask", Toast.LENGTH_SHORT).show();
                if (LocationService.isRecordingMode()) {
                    AskDialogFragment dialog = new AskDialogFragment();
                    dialog.show(getSupportFragmentManager(), "swaaag taaag");
                } else {
                    finish();
                }
                break;
        }
    }
}
