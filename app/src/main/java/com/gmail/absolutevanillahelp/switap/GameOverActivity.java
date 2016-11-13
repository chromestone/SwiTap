package com.gmail.absolutevanillahelp.switap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class GameOverActivity extends Activity implements View.OnClickListener {

    public final static String TAG_SCORE = "score";

    private int highscoreIndex;
    private JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_over);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        highscoreIndex = -1;

        String score = getIntent().getStringExtra(TAG_SCORE);

        ((TextView) findViewById(R.id.game_over_score_label)).setText(score);

        SharedPreferences sharedPreferences = getSharedPreferences(HighscoreActivity.PREF_FILE_NAME, MODE_PRIVATE);

        String arrayString = sharedPreferences.getString(HighscoreActivity.PREF_KEY, null);

        if (arrayString != null && !arrayString.isEmpty()) {

            try {

                JSONArray array = new JSONArray(arrayString);

                ArrayList<String> highScores = getStringArray(array);

                boolean positionFound = false;

                for (int i = 0; i < highScores.size(); i++) {

                    String highscore = highScores.get(i);

                    String[] split = highscore.split("\\|");

                    if (split.length < 2) {

                        continue;
                    }

                    if (Long.parseLong(score) > Long.parseLong(split[1])) {

                        highscoreIndex = i;
                        highScores.add(highscoreIndex, "|" + score);
                        positionFound = true;
                        break;
                    }
                }

                if (!positionFound && highScores.size() < HighscoreActivity.MING_E) {

                    highScores.add("|" + score);
                    highscoreIndex = highScores.size() - 1;
                }

                jsonArray = getJsonArray(highScores);
            }
            catch (JSONException e) {

                Log.wtf("SupermanvsBatman", "Error occured in GameOverActivity-rewriting highscores");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                jsonArray = new JSONArray();
            }
            catch (NumberFormatException e) {

                Log.wtf("SupermanvsBatman", "Error NumberFormat");
            }
        }
        else {

            jsonArray = new JSONArray();
            highscoreIndex = 0;
            try {

                jsonArray.put(highscoreIndex, "|" + score);
            }
            catch (JSONException e) {

                Log.wtf("SupermanvsBatman", "Error occured in GameOverActivity-first score not saved");
                highscoreIndex = -1;
            }
        }

        Button button = (Button) findViewById(R.id.game_over_continue_button);
        if (highscoreIndex < 0) {

            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.game_over_relativeLayout);
            viewGroup.removeView(viewGroup.findViewById(R.id.game_over_enter_name));
            viewGroup.removeView(viewGroup.findViewById(R.id.game_over_editText));
            button.setText(R.string.string_continue);
        }
        else {

            button.setText(R.string.save_continue);
        }
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (highscoreIndex >= 0) {


            String name = ((EditText) findViewById(R.id.game_over_editText)).getText().toString().trim();
            if (name.length() > 0) {

                try {

                    jsonArray.put(highscoreIndex, name + jsonArray.getString(highscoreIndex));
                }
                catch (JSONException e) {

                    Log.w("SupermanvsBatman", "Highscores NOT saved");
                }

                String arrayString = jsonArray.toString();

                SharedPreferences.Editor editor = getSharedPreferences(HighscoreActivity.PREF_FILE_NAME, MODE_PRIVATE)
                        .edit();
                editor.putString(HighscoreActivity.PREF_KEY, arrayString);
                editor.apply();

                Intent intent = new Intent(this, HighscoreActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(HighscoreActivity.TAG_LOADED_ARRAY, arrayString);
                intent.putExtra(HighscoreActivity.TAG_HIGHLIGHT, highscoreIndex);
                startActivity(intent);
                finish();
            }
            else {

                new AlertDialog.Builder(GameOverActivity.this)
                        .setMessage(R.string.no_name_warning)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                Intent intent = new Intent(GameOverActivity.this, HighscoreActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
        else {

            Intent intent = new Intent(GameOverActivity.this, HighscoreActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private ArrayList<String> getStringArray(JSONArray array) throws JSONException {

        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {

            Object o = array.get(i);
            if (o instanceof String) {

                temp.add((String) o);
            }
        }

        return temp;
    }

    public JSONArray getJsonArray(ArrayList<String> array) {

        JSONArray temp = new JSONArray();
        int i = 0;
        for (String str : array) {

            if (i > HighscoreActivity.MING_E) {

                break;
            }
            temp.put(str);
            i++;
        }
        return temp;
    }
}
