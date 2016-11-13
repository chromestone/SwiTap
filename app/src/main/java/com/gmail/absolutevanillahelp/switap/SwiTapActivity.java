package com.gmail.absolutevanillahelp.switap;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.os.*;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;

/**
 * Created by derekzhang on 7/28/15.
 */
public class SwiTapActivity extends Activity {

    public final static String TAG_PLAYER_1_NAME = "player_1_name";
    public final static String TAG_PLAYER_2_NAME = "player_2_name";
    public final static String TAG_TIME_LIMIT = "time_limit";

    private final static int MAX_PROGRESS = 10000;

    private VerticalProgressBar battleBar;

    private TextView player1TimeText;
    private TextView player2TimeText;

    private TextView player1TouchView;
    private TextView player2TouchView;

    private GestureDecider gestureDecider;

    private HandlerThread scoreHandlerThread;
    private Handler scoreHandler;

    private ScoreRecorder player1ScoreRecorder;
    private ScoreRecorder player2ScoreRecorder;

    private ReadySetGo readySetGo;

    private ResetableCountdownTimer countdownTimer;
    private volatile boolean timerFinished;

    private int progress;

    private boolean onFirstRun;

    private AlertDialog menuDialog;

    private SoundPool gestureSounds;
    private int[] soundIDs;
    private volatile boolean soundLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_switap);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ((TextView) findViewById(R.id.player1_name_label)).setMovementMethod(new ScrollingMovementMethod());
        ((TextView) findViewById(R.id.player2_name_label)).setMovementMethod(new ScrollingMovementMethod());

        battleBar = (VerticalProgressBar) findViewById(R.id.battle_bar);
        battleBar.setMax(MAX_PROGRESS);

        progress = MAX_PROGRESS / 2;
        battleBar.setProgress(progress);

        Intent intent = getIntent();

        ((TextView) findViewById(R.id.player1_name_label)).setText(intent.getStringExtra(TAG_PLAYER_1_NAME));
        ((TextView) findViewById(R.id.player2_name_label)).setText(intent.getStringExtra(TAG_PLAYER_2_NAME));

        player1TimeText = (TextView) findViewById(R.id.player1_time_label);
        player2TimeText = (TextView) findViewById(R.id.player2_time_label);

        player1TouchView = (TextView) findViewById(R.id.player1_touch_view);
        player2TouchView = (TextView) findViewById(R.id.player2_touch_view);

        gestureDecider = new GestureDecider(this);

        createScoreHandler();

        player1ScoreRecorder = new ScoreRecorder(this, 10);
        player2ScoreRecorder = new ScoreRecorder(this, 10);

        player1TouchView.setOnTouchListener(new GestureDetectorWrapper(new GestureDetector(this, player1ScoreRecorder)));
        player2TouchView.setOnTouchListener(new GestureDetectorWrapper(new GestureDetector(this, player2ScoreRecorder)));

        countdownTimer = new ResetableCountdownTimer(intent.getLongExtra(TAG_TIME_LIMIT, 30 * 1000), 1000);

        menuDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.game_paused)
                .setItems(R.array.menu_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 1) {

                            dialog.dismiss();
                            finish();
                        }
                        else {

                            createScoreHandler();
                            gestureDecider = new GestureDecider(SwiTapActivity.this);
                            countdownTimer = countdownTimer.reset();
                            readySetGo = new ReadySetGo();
                            readySetGo.start();
                        }
                    }
                })
                .setCancelable(false)
                .create();

        findViewById(R.id.menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        onFirstRun = true;

        soundLoaded = false;
    }

    @Override
    protected void onResume() {

        super.onResume();

        toggleTouchView(false);

        if (!onFirstRun) {

            player1TouchView.setText(R.string.touch_here);
            player2TouchView.setText(R.string.touch_here);

            if (!timerFinished) {

                menuDialog.show();
            }
        }


        if(Build.VERSION.SDK_INT >= 21){

            gestureSounds = new SoundPool.Builder()
                    .setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                    .build()
                    )
                    .setMaxStreams(1)
                    .build();
        }
        else {

            gestureSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        gestureSounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                if (status == 0 && soundIDs != null && soundIDs.length >= 5) {

                    if (sampleId == soundIDs[4]) {

                        soundLoaded = true;
                    }
                }
            }
        });
        soundIDs = new int[] {gestureSounds.load(this, R.raw.left, 1),
                gestureSounds.load(this, R.raw.right, 1),
                gestureSounds.load(this, R.raw.down, 1),
                gestureSounds.load(this, R.raw.up, 1),
                gestureSounds.load(this, R.raw.tap, 1)};
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if (onFirstRun && hasFocus) {

            readySetGo = new ReadySetGo();
            readySetGo.start();
            onFirstRun = false;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        if (menuDialog.isShowing()) {

            menuDialog.dismiss();
        }

        terminateProtocol();

        soundLoaded = false;
        gestureSounds.release();
    }

    @Override
    public void onBackPressed() {

        toggleTouchView(false);
        player1TouchView.setText(R.string.touch_here);
        player2TouchView.setText(R.string.touch_here);
        terminateProtocol();
        menuDialog.show();
    }

    private void terminateProtocol() {

        if (readySetGo != null) {

            readySetGo.cancel();
        }
        countdownTimer.cancel();
        scoreHandler = null;
        scoreHandlerThread.quitSafely();
        gestureDecider.interrupt();
    }

    private void createScoreHandler() {

        scoreHandlerThread = new HandlerThread("Score Handler Thread");
        scoreHandlerThread.start();

        scoreHandler = new Handler(scoreHandlerThread.getLooper());
    }

    public void playGestureSound(int id) {

        if (soundLoaded && id >= 0 && id < soundIDs.length) {

            gestureSounds.play(soundIDs[id], 1, 1, 0, 0, 1);
        }
    }

    protected void updateBattleBar() {

        long player1Score = player1ScoreRecorder.getScore();
        long combined = player1Score + player2ScoreRecorder.getScore();
        if (combined == Long.MAX_VALUE || combined < 2) {

            player1ScoreRecorder.setScore(progress == 0 ? 1 : progress);
            player2ScoreRecorder.setScore(MAX_PROGRESS - progress == 0 ? 1 : MAX_PROGRESS - progress);
        }
        else {

            progress = (int) ((double) player1Score / (combined) * MAX_PROGRESS);
            final int progress = this.progress;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    battleBar.setProgress(progress);
                }
            });
        }
    }

    protected void setTouchViewText(final String text) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                player1TouchView.setText(text);
                player2TouchView.setText(text);
            }
        });
    }

    public void runOnScoreThread(Runnable runnable) {

        if (scoreHandler != null) {

            scoreHandler.post(runnable);
        }
    }

    protected void updateGesture(final GestureDecider.Gesture gesture) {

        scoreHandler.post(new Runnable() {

            @Override
            public void run() {

                player1ScoreRecorder.setGesture(gesture);
                player2ScoreRecorder.setGesture(gesture);

            }
        });
    }

    private void setTimeDisplay(String time) {

        player1TimeText.setText(time);
        player2TimeText.setText(time);
    }

    private void toggleTouchView(boolean clickable) {

        player1TouchView.setEnabled(clickable);
        player2TouchView.setEnabled(clickable);
    }

    @Override
    public void finish() {

        if (timerFinished) {

            terminateProtocol();
            if (battleBar.getProgress() == MAX_PROGRESS / 2) {

                finisher(getResources().getString(R.string.tie));
            } else {

                String winner = "";
                if (battleBar.getProgress() < MAX_PROGRESS / 2) {

                    winner = getIntent().getStringExtra(TAG_PLAYER_2_NAME);
                } else {

                    winner = getIntent().getStringExtra(TAG_PLAYER_1_NAME);
                }

                finisher(winner + " " + getResources().getString(R.string.won));
            }
        }
        else {

            super.finish();
        }
    }

    private void finisher(String message) {

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        SwiTapActivity.super.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private class ResetableCountdownTimer extends CountDownTimer {

        private long totalTimeLeft;
        private final long interval;

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public ResetableCountdownTimer(long millisInFuture, long countDownInterval) {

            super(millisInFuture, countDownInterval);
            totalTimeLeft = millisInFuture;
            interval = countDownInterval;
            timerFinished = false;
        }

        private ResetableCountdownTimer(ResetableCountdownTimer resetableCountdownTimer) {

            super(resetableCountdownTimer.totalTimeLeft, resetableCountdownTimer.interval);
            totalTimeLeft = resetableCountdownTimer.totalTimeLeft;
            interval = resetableCountdownTimer.interval;
            timerFinished = false;
        }

        @Override
        public void onTick(long millisUntilFinished) {

            totalTimeLeft = millisUntilFinished;

            String str = SwiTapLauncher.convertToString(millisUntilFinished);
            final String time = str.substring(0, 2) + ":" + str.substring(2,4);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    setTimeDisplay(time);
                }
            });
        }

        @Override
        public void onFinish() {

            timerFinished = true;

            totalTimeLeft = 0;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    setTimeDisplay("00:00");
                    toggleTouchView(false);
                    finish();
                }
            });
        }

        public ResetableCountdownTimer reset() {

            return new ResetableCountdownTimer(this);
        }
    }

    private class ReadySetGo extends CountDownTimer {

        private int counter;

        public ReadySetGo() {

            super(3100, 1000);
            counter = 0;
        }

        @Override
        public void onTick(long millisUntilFinished) {

            final String message;
            if (counter == 0) {

                message = getResources().getString(R.string.ready);
            }
            else if (counter == 1) {

                message = getResources().getString(R.string.set);
            }
            else {

                message = getResources().getString(R.string.go);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    setTimeDisplay(message);
                }
            });

            counter++;
        }

        @Override
        public void onFinish() {

            gestureDecider.start();
            countdownTimer.start();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    toggleTouchView(true);
                }
            });

            counter = 0;
        }
    }
}
