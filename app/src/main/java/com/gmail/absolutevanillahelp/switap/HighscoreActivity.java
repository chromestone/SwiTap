package com.gmail.absolutevanillahelp.switap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

public class HighscoreActivity extends Activity {

    public final static String PREF_FILE_NAME = "com.gmail.absolutevanillahelp.switap.highscore";
    public final static String PREF_KEY = "highscore";
    public final static String TAG_LOADED_ARRAY = "loaded_array";
    public final static String TAG_HIGHLIGHT = "highlight";
    public final static int MING_E = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_highscore);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        findViewById(R.id.highscore_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        Intent intent = getIntent();

        String stringHighscores = intent.getStringExtra(TAG_LOADED_ARRAY);
        if (stringHighscores == null) {

            stringHighscores = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE)
                    .getString(PREF_KEY, null);
        }

        int i = 1, highlight = intent.getIntExtra(TAG_HIGHLIGHT, -1);

        TableLayout tableLayout = (TableLayout) findViewById(R.id.highscore_tableLayout);

        if (stringHighscores != null) {

            try {

                JSONArray jsonArray = new JSONArray(stringHighscores);

                for (int j = 0; j < jsonArray.length(); j++) {

                    String highscore = jsonArray.getString(j).trim();

                    if (i > MING_E) {

                        return;
                    }

                    String[] split = highscore.split("\\|");

                    if (split.length < 2) {

                        continue;
                    }

                    TableRow scoreProfile = (TableRow) this.getLayoutInflater().inflate(R.layout.score_profile, null);
                    ((TextView) scoreProfile.getChildAt(0)).setText(convertToString(i));
                    ((TextView) scoreProfile.getChildAt(1)).setText(split[0]);
                    ((TextView) scoreProfile.getChildAt(2)).setText(split[1]);
                    if (j == highlight) {

                        scoreProfile.setBackgroundColor(Color.BLUE);
                    }
                    tableLayout.addView(scoreProfile);
                    i++;
                }
            }
            catch (JSONException e) {}
        }
        for (; i <= MING_E; i++) {

            TableRow scoreProfile = (TableRow) this.getLayoutInflater().inflate(R.layout.score_profile, null);
            ((TextView) scoreProfile.getChildAt(0)).setText(convertToString(i));
            ((TextView) scoreProfile.getChildAt(1)).setText("-");
            ((TextView) scoreProfile.getChildAt(2)).setText("-");
            tableLayout.addView(scoreProfile);
        }
    }

    private String convertToString(int i) {

        String str = String.valueOf(i);
        if (str.length() > 2){

            str = str.substring(0, 2);
        }
        else {

            while (str.length() < 2) {

                str += " ";
            }
        }

        return str;
    }
}
