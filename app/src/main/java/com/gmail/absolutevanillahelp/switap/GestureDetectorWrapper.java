package com.gmail.absolutevanillahelp.switap;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by derekzhang on 1/19/16.
 */
public class GestureDetectorWrapper implements View.OnTouchListener {

    GestureDetector gestureDetector;

    public GestureDetectorWrapper(GestureDetector gestureDetector) {

        this.gestureDetector = gestureDetector;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return gestureDetector.onTouchEvent(event);
    }
}
