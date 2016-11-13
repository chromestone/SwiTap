package com.gmail.absolutevanillahelp.switap;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.*;

/**
 * Created by derekzhang on 7/27/15.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        findViewById(R.id.single_player_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.this.startActivity(new Intent(MainActivity.this, SinglePlayerLauncher.class));
            }
        });

        findViewById(R.id.multiplayer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.this.startActivity(new Intent(MainActivity.this, SwiTapLauncher.class));
            }
        });
    }
}
