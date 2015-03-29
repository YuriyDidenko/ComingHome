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

public class Logo extends Activity {
    private Intent intentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logo);
        intentService = new Intent(this, LocationService.class);
        startService(intentService);

        ImageView imgLogo = (ImageView) findViewById(R.id.img_logo);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Logo.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        imgLogo.startAnimation(animation);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(App.TAG, "Logo: onStop");
        //stopService(intentService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intentService);
    }
}
