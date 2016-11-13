package com.gmail.absolutevanillahelp.switap;

import android.os.Looper;
import android.util.Log;
import android.view.*;

/**
 * Created by derekzhang on 8/3/15.
 */
public class ScoreRecorder extends GestureDetector.SimpleOnGestureListener {

    private final SwiTapActivity swiTapActivity;
    private final float minSwipeVelocity;
    private long score;
    private GestureDecider.Gesture gesture;

    public ScoreRecorder(SwiTapActivity swiTapActivity, float minSwipeVelocity) {

        this.swiTapActivity = swiTapActivity;
        this.minSwipeVelocity = minSwipeVelocity;
        score = 1;
        gesture = null;
    }

    public long getScore() {

        return score;
    }

    public void setScore(long score) {

        this.score = score;
    }

    private void updateScore(boolean b) {

        if (b) {

            score++;
            swiTapActivity.updateBattleBar();
        }
        else if (score > 1) {

            score--;
            swiTapActivity.updateBattleBar();
        }
    }

    public void setGesture(GestureDecider.Gesture gesture) {

        this.gesture = gesture;
    }

    public GestureDecider.Gesture getGesture() {

        return gesture;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2,
                           final float velocityX, final float velocityY) {

        swiTapActivity.runOnScoreThread(new Runnable() {

            @Override
            public void run() {

                GestureDecider.Gesture gesture = getGesture();
                if (gesture != null) {

                    onFling(e1, e2, velocityX, velocityY, gesture);
                }

            }
        });
        return true;
    }

    private void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY,
                         GestureDecider.Gesture gesture) {

        float xDistance = Math.abs(e1.getX() - e2.getX());
        float yDistance = Math.abs(e1.getY() - e2.getY());

        velocityX = Math.abs(velocityX);
        velocityY = Math.abs(velocityY);

        if(velocityX > minSwipeVelocity && xDistance > yDistance){
            if(e1.getX() > e2.getX()) { // right to left

                updateScore(GestureDecider.Gesture.LEFT == gesture);
            }
            else {

                updateScore(GestureDecider.Gesture.RIGHT == gesture);
            }
        }
        else if(velocityY > minSwipeVelocity && yDistance > xDistance){
            if(e1.getY() > e2.getY()) { // bottom to up

                updateScore(GestureDecider.Gesture.UP == gesture);
            }
            else {

                updateScore(GestureDecider.Gesture.DOWN == gesture);
            }
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        swiTapActivity.runOnScoreThread(new Runnable() {

            @Override
            public void run() {

                GestureDecider.Gesture gesture = getGesture();
                if (gesture != null) {

                    onSingleTapUp(gesture);
                }

            }
        });
        return true;
    }

    private void onSingleTapUp(GestureDecider.Gesture gesture) {

        if (GestureDecider.Gesture.TAP == gesture) {

            score++;
            swiTapActivity.updateBattleBar();
        }
        else if (score > 1) {

            score--;
            swiTapActivity.updateBattleBar();
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {

        return true;
    }
}