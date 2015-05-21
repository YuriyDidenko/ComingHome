package example.com.cominghome.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import example.com.cominghome.R;
import example.com.cominghome.app.App;
import example.com.cominghome.background.LocationService;
import example.com.cominghome.data.DBManager;
import example.com.cominghome.data.RouteTable;

public class Logo extends Activity {
    private RouteTable routeTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logo);
        Log.d(App.TAG, "Logo: onCreate");

        Intent intentService = new Intent(this, LocationService.class);
        if (!App.isServiceRunning(this, LocationService.class)) {
            Log.d(App.TAG, "Logo: Service was inactive, it's running now");
            startService(intentService);
        } else {
            Log.d(App.TAG, "Logo: Service is enabled");
            Intent intent = new Intent(Logo.this, MapsActivity.class);
            startActivity(intent);
            Logo.this.finish();
        }

        ImageView imgLogo = (ImageView) findViewById(R.id.img_logo);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Logo.this, MapsActivity.class);
                startActivity(intent);
                Logo.this.finish();
            }
        });

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        imgLogo.startAnimation(animation);

        routeTable = DBManager.getHelper().getRouteTable();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(App.TAG, "Logo: onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        //Log.d(App.TAG, "Logo b_start getMe ="+ App.getMe());
        super.onStart();
//        Log.d(App.TAG, "Logo a_start getMe ="+ App.getMe());
    }

    @Override
    protected void onRestart() {
//        Log.d(App.TAG, "Logo b_restart getMe ="+ App.getMe());
        super.onRestart();
//        Log.d(App.TAG, "Logo a_restart getMe ="+ App.getMe());
    }

    @Override
    protected void onResume() {
//        Log.d(App.TAG, "Logo b_resume getMe ="+ App.getMe());
        super.onResume();
//        Log.d(App.TAG, "Logo a_resume getMe ="+ App.getMe());
        Log.d(App.TAG, routeTable.getFullRoute().toString());
    }

    @Override
    protected void onPause() {
//        Log.d(App.TAG, "Logo b_pause getMe ="+ App.getMe());
        super.onPause();
//        Log.d(App.TAG, "Logo a_pause getMe ="+ App.getMe());
    }
}
