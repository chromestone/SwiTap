package com.gmail.absolutevanillahelp.switap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SinglePlayerActivity extends Activity {

    private int maxProgress;

    private VerticalProgressBar timeBar;

    private TextView scoreText;

    private int score;

    private TextView playerTouchView;
    private GestureListener gestureListener;

    private ReadySetGo readySetGo;

    private MyCountdownTimer countdownTimer;

    private TextView playerNotify;

    private AlertDialog menuDialog;

    private boolean onFirstRun;

    private boolean gameOver;

    private SoundPool gestureSounds;
    private int[] soundIDs;
    private volatile boolean soundLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_player);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        maxProgress = 3000;

        timeBar = (VerticalProgressBar) findViewById(R.id.time_bar);
        timeBar.setMax(maxProgress);
        timeBar.setProgress(maxProgress);

        scoreText = (TextView) findViewById(R.id.score_label);
        score = 0;

        playerTouchView = (TextView) findViewById(R.id.single_player_touch_view);
        gestureListener = new GestureListener(10);
        playerTouchView.setOnTouchListener(new GestureDetectorWrapper(new GestureDetector(this, gestureListener)));

        countdownTimer = new MyCountdownTimer(maxProgress, 1);

        playerNotify = (TextView) findViewById(R.id.single_player_notify);

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

                            timeBar.getProgressDrawable()
                                    .setColorFilter(
                                            new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN));
                            timeBar.setProgress(maxProgress);
                            countdownTimer = new MyCountdownTimer(maxProgress, 1);
                            readySetGo = new ReadySetGo();
                            readySetGo.start();
                        }
                    }
                })
                .setCancelable(false)
                .create();

        findViewById(R.id.single_player_menuView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        onFirstRun = true;

        gameOver = false;

        soundLoaded = false;
    }

    @Override
    protected void onResume() {

        super.onResume();

        playerTouchView.setEnabled(false);

        if (!onFirstRun) {

            playerTouchView.setText(R.string.touch_here);

            if (!gameOver) {

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

        playerTouchView.setEnabled(false);
        playerTouchView.setText(R.string.touch_here);
        terminateProtocol();
        menuDialog.show();
    }

    private void terminateProtocol() {

        if (readySetGo != null) {

            readySetGo.cancel();
        }
        countdownTimer.cancel();
    }

    private void incrementScore() {

        countdownTimer.cancel();

        playerTouchView.setEnabled(false);

        if (maxProgress > 1500) {

            maxProgress -= 20;
        }
        else if (maxProgress > 1000) {

            maxProgress -= 10;
        }
        else if (maxProgress > 500) {

            maxProgress -= 1;
        }
        timeBar.getProgressDrawable()
                .setColorFilter(
                        new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN));
        timeBar.setMax(maxProgress);
        timeBar.setProgress(maxProgress);

        countdownTimer = new MyCountdownTimer(maxProgress, 1);

        score += 10;
        scoreText.setText(String.valueOf(score));

        playGestureSound(gestureListener.renewGesture());
        displayGesture();

        playerTouchView.setEnabled(true);

        countdownTimer.start();

    }

    private void displayGesture() {

        GestureDecider.Gesture gesture = gestureListener.gesture;
        String message = "";
        if (gesture != GestureDecider.Gesture.TAP) {
            message = getResources().getString(R.string.swipe) + " ";
        }

        playerTouchView.setText(message + gesture);
    }

    private void decrementScore() {

        score -= 1;
        if (score < 0) {

            score = 0;
        }
        scoreText.setText(String.valueOf(score));
    }

    public void playGestureSound(int id) {

        if (soundLoaded && id >= 0 && id < soundIDs.length) {

            gestureSounds.play(soundIDs[id], 1, 1, 0, 0, 1);
        }
    }

    @Override
    public void finish() {

        if (gameOver) {

            playerTouchView.setEnabled(false);
            terminateProtocol();

            Intent intent = new Intent(SinglePlayerActivity.this, GameOverActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(GameOverActivity.TAG_SCORE, String.valueOf(score));
            startActivity(intent);

            SinglePlayerActivity.super.finish();
        }
        else {

            super.finish();
        }
    }

    private class MyCountdownTimer extends CountDownTimer {

        private long totalTimeLeft;
        private boolean yellow;
        private boolean red;
        private Accumulator accumulator;

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public MyCountdownTimer(long millisInFuture, long countDownInterval) {

            super(millisInFuture, countDownInterval);
            totalTimeLeft = millisInFuture;
            yellow = false;
            red = false;
            accumulator = new Accumulator(300);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            accumulator.accumulate(totalTimeLeft - millisUntilFinished);

            totalTimeLeft = millisUntilFinished;

            final int timeLeft = Math.max(Math.min((int) millisUntilFinished, maxProgress), 0);

            if (!(timeLeft < .2 * maxProgress)) {

                if (!yellow && timeLeft < .6 * maxProgress) {

                    yellow = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            timeBar.getProgressDrawable()
                                    .setColorFilter(
                                            new PorterDuffColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN));
                        }
                    });
                }
            }
            else if (!red) {

                red = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        timeBar.getProgressDrawable()
                                .setColorFilter(
                                        new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
                    }
                });
            }

            final boolean decrementScore = accumulator.removeTotal();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    timeBar.setProgress(timeLeft);
                    if (decrementScore) {

                        decrementScore();
                    }
                }
            });
        }

        @Override
        public void onFinish() {

            totalTimeLeft = 0;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    gameOver = true;
                    finish();
                }
            });
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
            } else if (counter == 1) {

                message = getResources().getString(R.string.set);
            } else {

                message = getResources().getString(R.string.go);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    playerNotify.setText(message);
                }
            });

            counter++;
        }

        @Override
        public void onFinish() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    playerNotify.setText("");
                    playGestureSound(gestureListener.renewGesture());
                    displayGesture();
                    playerTouchView.setEnabled(true);
                    countdownTimer.start();
                }
            });

            counter = 0;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private final float minSwipeVelocity;
        private GestureDecider.Gesture gesture;

        public GestureListener(float minSwipeVelocity) {

            this.minSwipeVelocity = minSwipeVelocity;
            gesture = null;
        }

        public int renewGesture() {

            int selection = (int) (Math.random() * GestureDecider.Gesture.values().length);
            gesture = GestureDecider.Gesture.values()[selection];
            return selection;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {


            float xDistance = Math.abs(e1.getX() - e2.getX());
            float yDistance = Math.abs(e1.getY() - e2.getY());

            velocityX = Math.abs(velocityX);
            velocityY = Math.abs(velocityY);

            boolean gestureMatches = false;

            if(velocityX > minSwipeVelocity && xDistance > yDistance){
                if(e1.getX() > e2.getX()) { // right to left

                    gestureMatches = (GestureDecider.Gesture.LEFT == gesture);
                }
                else {

                    gestureMatches = (GestureDecider.Gesture.RIGHT == gesture);
                }
            }
            else if(velocityY > minSwipeVelocity && yDistance > xDistance){
                if(e1.getY() > e2.getY()) { // bottom to up

                    gestureMatches = (GestureDecider.Gesture.UP == gesture);
                }
                else {

                    gestureMatches = (GestureDecider.Gesture.DOWN == gesture);
                }
            }

            if (gestureMatches) {

                incrementScore();
            }
            else {

                gameOver = true;
                finish();
            }

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            if (GestureDecider.Gesture.TAP == gesture) {

                incrementScore();
            }
            else {

                gameOver = true;
                finish();
            }

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }
    }
}
