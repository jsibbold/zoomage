package com.jsibbold.zoomage;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Default implementation of GestureListener for {@link ZoomageView}.
 * If you want to change it in {@link ZoomageView} - override method {@link ZoomageView#getOnGestureListener()}
 * and provide your own implementation
 */
public class OnGestureListener extends GestureDetector.SimpleOnGestureListener {

    private final ZoomageView view;

    public OnGestureListener(ZoomageView view) {
        this.view = view;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (view != null && e.getAction() == MotionEvent.ACTION_UP) {
            view.setDoubleTapDetected(true);
        }
        notifySingleTapDetection(true);
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        notifySingleTapDetection(true);
        return false;
    }

    private void notifySingleTapDetection(boolean detected) {
        if (view != null) {
            view.setSingleTapDetected(detected);
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        notifySingleTapDetection(false);
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }
}
