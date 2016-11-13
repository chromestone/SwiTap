package com.gmail.absolutevanillahelp.switap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

public class SinglePlayerLauncher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_player_launcher);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        findViewById(R.id.single_player_go_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SinglePlayerLauncher.this, SinglePlayerActivity.class));
                //SinglePlayerLauncher.this.startActivityForResult(new Intent(SinglePlayerLauncher.this, SinglePlayerActivity.class), 12345);
            }
        });

        findViewById(R.id.highscore_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SinglePlayerLauncher.this, HighscoreActivity.class));
            }
        });

        findViewById(R.id.single_player_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK && requestCode == 12345) {
//
//            SinglePlayerLauncher.this.startActivity(new Intent(SinglePlayerLauncher.this, HighscoreActivity.class));
//        }
//    }
}
