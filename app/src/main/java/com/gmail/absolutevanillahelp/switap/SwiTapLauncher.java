package com.gmail.absolutevanillahelp.switap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class SwiTapLauncher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switap_launcher);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        final Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
        final Spinner spinner4 = (Spinner) findViewById(R.id.spinner4);

        final EditText player1EditText = (EditText) findViewById(R.id.player1_editText);
        final EditText player2EditText = (EditText) findViewById(R.id.player2_editText);

        ArrayAdapter<CharSequence> toFiveAdapter = ArrayAdapter.createFromResource(
                this, R.array.to_five, R.layout.dropdown_item);

        spinner1.setAdapter(toFiveAdapter);
        spinner3.setAdapter(toFiveAdapter);

        ArrayAdapter<CharSequence> toNineAdapter = ArrayAdapter.createFromResource(
                this, R.array.to_nine, R.layout.dropdown_item);

        spinner2.setAdapter(toNineAdapter);
        spinner4.setAdapter(toNineAdapter);

        spinner3.setSelection(3);

        findViewById(R.id.go_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String timeLimit = spinner1.getSelectedItem().toString()
                        + spinner2.getSelectedItem().toString()
                        + spinner3.getSelectedItem().toString()
                        + spinner4.getSelectedItem().toString();
                if (timeLimit.equals("0000")) {

                    new AlertDialog.Builder(SwiTapLauncher.this)
                            .setMessage(R.string.invalid_time_warning)
                            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
                            {@Override public void onClick(DialogInterface dialog, int which) {}})
                            .setCancelable(false)
                            .show();
                    return;
                }
                String player1Name = player1EditText.getText().toString().trim();
                String player2Name = player2EditText.getText().toString().trim();
                if (player1Name.length() <= 0
                        || player2Name.length() <= 0) {

                    new AlertDialog.Builder(SwiTapLauncher.this)
                            .setMessage(R.string.invalid_name_warning)
                            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
                            {@Override public void onClick(DialogInterface dialog, int which) {}})
                            .setCancelable(false)
                            .show();
                    return;
                }
                Intent intent = new Intent(SwiTapLauncher.this, SwiTapActivity.class);
                intent.putExtra(SwiTapActivity.TAG_PLAYER_1_NAME, player1Name);
                intent.putExtra(SwiTapActivity.TAG_PLAYER_2_NAME, player2Name);
                intent.putExtra(SwiTapActivity.TAG_TIME_LIMIT, convertToMilliseconds(timeLimit));
                SwiTapLauncher.this.startActivity(intent);
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    public static long convertToMilliseconds(String convert) {

        convert = makeItLength4(convert);
        return (Integer.parseInt(convert.substring(0, 2)) * 60
                + Integer.parseInt(convert.substring(2, 4))) * 1000;
    }

    public static String convertToString(long milliseconds) {

        milliseconds /= 1000;
        return makeItLength4(String.valueOf(milliseconds / 60) + String.valueOf(milliseconds % 60));
    }

    private static String makeItLength4(String str) {

        if (str.length() < 4) {

            while (str.length() < 4) {

                str = "0" + str;
            }
        }
        return str;
    }
}
