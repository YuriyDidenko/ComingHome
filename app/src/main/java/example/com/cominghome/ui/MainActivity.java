package example.com.cominghome.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;

import example.com.cominghome.R;
import example.com.cominghome.ui.fragments.AboutFragment;
import example.com.cominghome.ui.fragments.MapsFragment;
import example.com.cominghome.ui.fragments.OptionsFragment;
import example.com.cominghome.utils.DrawerItem;
import example.com.cominghome.utils.DrawerItemAdapter;
import example.com.cominghome.utils.Utils;

public class MainActivity extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        DrawerItem[] drawerItem = new DrawerItem[5];

        drawerItem[0] = new DrawerItem(R.drawable.abc_switch_track_mtrl_alpha, "Карта");
        drawerItem[1] = new DrawerItem(R.drawable.abc_switch_track_mtrl_alpha, "Вид карты");
        drawerItem[2] = new DrawerItem(R.drawable.abc_switch_track_mtrl_alpha, "Reset");
        drawerItem[3] = new DrawerItem(R.drawable.abc_switch_track_mtrl_alpha, "Options");
        drawerItem[4] = new DrawerItem(R.drawable.abc_switch_track_mtrl_alpha, "About");

        DrawerItemAdapter adapter = new DrawerItemAdapter(this, R.layout.drawer_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);

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
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        currentFragment = new MapsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private void selectItem(int position) {

        switch (position) {
            // map
            case 0:
                if (!(currentFragment instanceof MapsFragment))
                    currentFragment = new MapsFragment();
                break;
            // map view
            case 1:
                // тоже что и пред., но передавать в бандл например тип карты
                Bundle bundle = new Bundle();
                bundle.putInt(Utils.MAP_TYPE_KEY, GoogleMap.MAP_TYPE_HYBRID);
                currentFragment = new MapsFragment();
                currentFragment.setArguments(bundle);
                break;
            // reset
            case 2:
                // хуй его знает
                currentFragment = null;
                break;
            // options
            case 3:
                // продумать options
                currentFragment = new OptionsFragment();
                break;
            // about
            case 4:
                currentFragment = new AboutFragment();
                break;
            default:
                break;
        }

        if (currentFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }
}
