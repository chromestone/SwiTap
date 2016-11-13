package com.gmail.absolutevanillahelp.switap;

/**
 * Created by derekzhang on 7/29/15.
 */
public class GestureDecider extends Thread {

    enum Gesture {
        LEFT, RIGHT, DOWN, UP, TAP
    }

    private final SwiTapActivity activity;

    public GestureDecider(SwiTapActivity activity) {

        this.activity = activity;
    }

    @Override
    public void run() {

        try {
            while (!isInterrupted()) {

                int selection = (int) (Math.random() * Gesture.values().length);
                Gesture gesture = Gesture.values()[selection];

                activity.updateGesture(gesture);

                activity.playGestureSound(selection);

                String message = "";
                if (gesture != Gesture.TAP) {
                    message = activity.getResources().getString(R.string.swipe) + " ";
                }
                activity.setTouchViewText(message + gesture);

                randomSleep();
            }
        }catch (InterruptedException ignore) {

        }
    }

    private void randomSleep() throws InterruptedException {

        int seconds = (int) (Math.random() * 6) + 2;
        Thread.sleep(seconds * 1000);
    }
}
