package com.devel.ameyaapte1.androinput;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by ameyaapte1 on 26/2/17.
 */

public class CustomGestureHandler {
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout() + 100;
    private MultiTouchListener gestureListener;
    private long firtDownTime = 0;
    private boolean isGestureHandled;

    public CustomGestureHandler(MultiTouchListener gestureListener) {
        if (gestureListener == null) {
            throw new IllegalArgumentException("NullPointerException");
        }
        this.gestureListener = gestureListener;
    }

    public boolean TapListener(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isGestureHandled = false;
                firtDownTime = event.getEventTime();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int count = event.getPointerCount();
                if (event.getEventTime() - firtDownTime <= TAP_TIMEOUT) {
                    if (count == 3) {
                        if (!isGestureHandled) {
                            isGestureHandled = gestureListener.onTripleFingerTap(event);
                        }
                    } else if (count == 2) {
                        if (!isGestureHandled) {
                            isGestureHandled = gestureListener.onDoubleFingerTap(event);
                        }
                    }
                }
                firtDownTime = 0;
                break;
        }
        return isGestureHandled;
    }

    public interface MultiTouchListener {

        boolean onTripleFingerTap(MotionEvent ev);

        boolean onDoubleFingerTap(MotionEvent ev);
    }
}
