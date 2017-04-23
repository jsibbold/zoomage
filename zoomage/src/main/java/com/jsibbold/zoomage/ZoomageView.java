/**
 * Copyright 2016 Jeffrey Sibbold
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jsibbold.zoomage;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.ImageView;

/**
 * ZoomageView is a pinch-to-zoom extension of {@link ImageView}, providing a smooth
 * user experience and a very natural feel when zooming and translating. It also supports
 * automatic resetting, and allows for exterior bounds restriction to keep the image within
 * visible window.
 */
public class ZoomageView extends AppCompatImageView implements OnScaleGestureListener {

    private final float MIN_SCALE = 0.6f;
    private final float MAX_SCALE = 8f;
    private final int RESET_DURATION = 200;

    private ScaleType startScaleType;

    // These matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix startMatrix = new Matrix();

    private float[] matrixValues = new float[9];
    private float[] startValues = null;

    private float minScale = MIN_SCALE;
    private float maxScale = MAX_SCALE;

    //the adjusted scale bounds that account for an image's starting scale values
    private float calculatedMinScale = MIN_SCALE;
    private float calculatedMaxScale = MAX_SCALE;

    private final RectF bounds = new RectF();

    private boolean translatable;
    private boolean zoomable;
    private boolean restrictBounds;
    private boolean animateOnReset;
    private boolean autoCenter;
    @AutoResetMode private int autoResetMode;

    private PointF last = new PointF(0, 0);
    private float startScale = 1f;
    private float scaleBy = 1f;
    private int previousPointerCount = 1;

    private ScaleGestureDetector scaleDetector;

    public ZoomageView(Context context) {
        this(context, null);
    }

    public ZoomageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scaleDetector = new ScaleGestureDetector(context, this);
        ScaleGestureDetectorCompat.setQuickScaleEnabled(scaleDetector, false);
        startScaleType = getScaleType();

        TypedArray values = context.obtainStyledAttributes(attrs, com.jsibbold.zoomage.R.styleable.ZoomageView);

        zoomable = values.getBoolean(com.jsibbold.zoomage.R.styleable.ZoomageView_zoomage_zoomable, true);
        translatable = values.getBoolean(com.jsibbold.zoomage.R.styleable.ZoomageView_zoomage_translatable, true);
        animateOnReset = values.getBoolean(com.jsibbold.zoomage.R.styleable.ZoomageView_zoomage_animateOnReset, true);
        autoCenter = values.getBoolean(R.styleable.ZoomageView_zoomage_autoCenter, true);
        restrictBounds = values.getBoolean(com.jsibbold.zoomage.R.styleable.ZoomageView_zoomage_restrictBounds, false);
        minScale = values.getFloat(com.jsibbold.zoomage.R.styleable.ZoomageView_zoomage_minScale, MIN_SCALE);
        maxScale = values.getFloat(com.jsibbold.zoomage.R.styleable.ZoomageView_zoomage_maxScale, MAX_SCALE);
        autoResetMode = AutoResetMode.Parser.fromInt(values.getInt(com.jsibbold.zoomage.R.styleable.ZoomageView_zoomage_autoResetMode, AutoResetMode.UNDER));

        verifyScaleRange();

        values.recycle();
    }

    private void verifyScaleRange() {
        if (minScale >= maxScale) {
            throw new IllegalStateException("minScale must be less than maxScale");
        }

        if (minScale < 0) {
            throw new IllegalStateException("minScale must be greater than 0");
        }

        if (maxScale < 0) {
            throw new IllegalStateException("maxScale must be greater than 0");
        }
    }

    /**
     * Set the minimum and maximum allowed scale for zooming. {@code minScale} cannot
     * be greater than {@code maxScale} and neither can be 0 or less. This will result
     * in an {@link IllegalStateException}.
     * @param minScale minimum allowed scale
     * @param maxScale maximum allowed scale
     */
    public void setScaleRange(final float minScale, final float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;

        startValues = null;

        verifyScaleRange();
    }

    /**
     * Returns whether the image is translatable.
     * @return true if translation of image is allowed, false otherwise
     */
    public boolean isTranslatable() {
        return translatable;
    }

    /**
     * Set the image's translatable state.
     * @param translatable true to enable translation, false to disable it
     */
    public void setTranslatable(boolean translatable) {
        this.translatable = translatable;
    }

    /**
     * Returns the zoomable state of the image.
     * @return true if pinch-zooming of the image is allowed, false otherwise.
     */
    public boolean isZoomable() {
        return zoomable;
    }

    /**
     * Set the zoomable state of the image.
     * @param zoomable true to enable pinch-zooming of the image, false to disable it
     */
    public void setZoomable(final boolean zoomable) {
        this.zoomable = zoomable;
    }

    /**
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     * @return true if image bounds are restricted to the view's edges, false otherwise
     */
    public boolean getRestrictBounds() {
        return restrictBounds;
    }

    /**
     * Set the restrictBounds status of the image.
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     * @param restrictBounds true if image bounds should be restricted to the view's edges, false otherwise
     */
    public void setRestrictBounds(final boolean restrictBounds) {
        this.restrictBounds = restrictBounds;
    }

    /**
     * Returns status of animateOnReset. This causes the image to smoothly animate back
     * to its start position when reset. Default value is true.
     * @return true if animateOnReset is enabled, false otherwise
     */
    public boolean getAnimateOnReset() {
        return animateOnReset;
    }

    /**
     * Set whether or not the image should animate when resetting.
     * @param animateOnReset true if image should animate when resetting, false to snap
     */
    public void setAnimateOnReset(final boolean animateOnReset) {
        this.animateOnReset = animateOnReset;
    }

    /**
     * Get the current {@link AutoResetMode} mode of the image. Default value is {@link AutoResetMode#UNDER}.
     * @return the current {@link AutoResetMode} mode, one of {@link AutoResetMode#OVER OVER}, {@link AutoResetMode#UNDER UNDER},
     * {@link AutoResetMode#ALWAYS ALWAYS}, or {@link AutoResetMode#NEVER NEVER}
     */
    @AutoResetMode
    public int getAutoResetMode() {
        return autoResetMode;
    }

    /**
     * Set the {@link AutoResetMode} mode for the image.
     * @param autoReset the desired mode, one of {@link AutoResetMode#OVER OVER}, {@link AutoResetMode#UNDER UNDER},
     * {@link AutoResetMode#ALWAYS ALWAYS}, or {@link AutoResetMode#NEVER NEVER}
     */
    public void setAutoResetMode(@AutoResetMode final int autoReset) {
        this.autoResetMode = autoReset;
    }

    /**
     * Whether or not the image should automatically center itself when it's dragged partially or
     * fully out of view.
     * @return true if image should center itself automatically, false if it should not
     */
    public boolean getAutoCenter() {
        return autoCenter;
    }

    /**
     * Set whether or not the image should automatically center itself when it's dragged
     * partially or fully out of view.
     * @param autoCenter true if image should center itself automatically, false if it should not
     */
    public void setAutoCenter(final boolean autoCenter) {
        this.autoCenter = autoCenter;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        super.setScaleType(scaleType);
        startScaleType = scaleType;
        startValues = null;
    }

    /**
     * Set enabled state of the view. Note that this will reset the image's
     * {@link android.widget.ImageView.ScaleType} to its pre-zoom state.
     * @param enabled enabled state
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            setScaleType(startScaleType);
        }
    }

    /**
     * Update the bounds of the displayed image based on the current matrix.
     *
     * @param values the image's current matrix values.
     */
    private void updateBounds(final float[] values) {
        if (getDrawable() != null) {
            bounds.set(values[Matrix.MTRANS_X],
                    values[Matrix.MTRANS_Y],
                    getDrawable().getIntrinsicWidth() * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X],
                    getDrawable().getIntrinsicHeight() * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y]);
        }
    }

    /**
     * Get the width of the displayed image.
     *
     * @return the current width of the image as displayed (not the width of the {@link ImageView} itself.
     */
    private float getCurrentDisplayedWidth() {
        if (getDrawable() != null)
            return getDrawable().getIntrinsicWidth() * matrixValues[Matrix.MSCALE_X];
        else
            return 0;
    }

    /**
     * Get the height of the displayed image.
     *
     * @return the current height of the image as displayed (not the height of the {@link ImageView} itself.
     */
    private float getCurrentDisplayedHeight() {
        if (getDrawable() != null)
            return getDrawable().getIntrinsicHeight() * matrixValues[Matrix.MSCALE_Y];
        else
            return 0;
    }

    /**
     * Remember our starting values so we can animate our image back to its original position.
     */
    private void setStartValues() {
        startValues = new float[9];
        startMatrix = new Matrix(getImageMatrix());
        startMatrix.getValues(startValues);
        calculatedMinScale = minScale * startValues[Matrix.MSCALE_X];
        calculatedMaxScale = maxScale * startValues[Matrix.MSCALE_X];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isEnabled() && (zoomable || translatable)) {
            if (getScaleType() != ScaleType.MATRIX) {
                super.setScaleType(ScaleType.MATRIX);
            }

            if (startValues == null) {
                setStartValues();
            }

            //get the current state of the image matrix, its values, and the bounds of the drawn bitmap
            matrix.set(getImageMatrix());
            matrix.getValues(matrixValues);
            updateBounds(matrixValues);

            scaleDetector.onTouchEvent(event);

            /* if the event is a down touch, or if the number of touch points changed,
            * we should reset our start point, as event origins have likely shifted to a
            * different part of the screen*/
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                    event.getPointerCount() != previousPointerCount) {
                last.set(scaleDetector.getFocusX(), scaleDetector.getFocusY());
            }
            else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

                final float focusx = scaleDetector.getFocusX();
                final float focusy = scaleDetector.getFocusY();

                if (translatable) {
                    //calculate the distance for translation
                    float xdistance = getXDistance(focusx, last.x);
                    float ydistance = getYDistance(focusy, last.y);
                    matrix.postTranslate(xdistance, ydistance);
                }

                if (zoomable) {
                    matrix.postScale(scaleBy, scaleBy, focusx, focusy);
                }

                setImageMatrix(matrix);

                last.set(focusx, focusy);
            }

            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                scaleBy = 1f;
                resetImage();
            }

            //this tracks whether they have changed the number of fingers down
            previousPointerCount = event.getPointerCount();

            return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Reset the image based on the specified {@link AutoResetMode} mode.
     */
    private void resetImage() {
        switch (autoResetMode) {
            case AutoResetMode.UNDER:
                if (matrixValues[Matrix.MSCALE_X] <= startValues[Matrix.MSCALE_X]) {
                    reset();
                } else {
                    center();
                }
                break;
            case AutoResetMode.OVER:
                if (matrixValues[Matrix.MSCALE_X] >= startValues[Matrix.MSCALE_X]) {
                    reset();
                } else {
                    center();
                }
                break;
            case AutoResetMode.ALWAYS:
                reset();
                break;
            case AutoResetMode.NEVER:
                center();
        }
    }

    /**
     * This helps to keep the image on-screen by animating the translation to the nearest
     * edge, both vertically and horizontally.
     */
    private void center() {
        if (autoCenter) {
            animateTranslationX();
            animateTranslationY();
        }
    }

    /**
     * Reset image back to its original size. Will snap back to original size
     * if animation on reset is disabled via {@link #setAnimateOnReset(boolean)}.
     */
    public void reset() {
        reset(animateOnReset);
    }

    /**
     * Reset image back to its starting size. If {@code animate} is false, image
     * will snap back to its original size.
     * @param animate animate the image back to its starting size
     */
    public void reset(final boolean animate) {
        if (animate) {
            animateToStartMatrix();
        }
        else {
            setImageMatrix(startMatrix);
        }
    }

    /**
     * Animate the matrix back to its original position after the user stopped interacting with it.
     */
    private void animateToStartMatrix() {

        final Matrix beginMatrix = new Matrix(getImageMatrix());
        beginMatrix.getValues(matrixValues);

        //difference in current and original values
        final float xsdiff = startValues[Matrix.MSCALE_X] - matrixValues[Matrix.MSCALE_X];
        final float ysdiff = startValues[Matrix.MSCALE_Y] - matrixValues[Matrix.MSCALE_Y];
        final float xtdiff = startValues[Matrix.MTRANS_X] - matrixValues[Matrix.MTRANS_X];
        final float ytdiff = startValues[Matrix.MTRANS_Y] - matrixValues[Matrix.MTRANS_Y];

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1f);
        anim.addUpdateListener(new AnimatorUpdateListener() {

            final Matrix activeMatrix = new Matrix(getImageMatrix());
            final float[] values = new float[9];

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (Float) animation.getAnimatedValue();
                activeMatrix.set(beginMatrix);
                activeMatrix.getValues(values);
                values[Matrix.MTRANS_X] = values[Matrix.MTRANS_X] + xtdiff * val;
                values[Matrix.MTRANS_Y] = values[Matrix.MTRANS_Y] + ytdiff * val;
                values[Matrix.MSCALE_X] = values[Matrix.MSCALE_X] + xsdiff * val;
                values[Matrix.MSCALE_Y] = values[Matrix.MSCALE_Y] + ysdiff * val;
                activeMatrix.setValues(values);
                setImageMatrix(activeMatrix);
            }
        });
        anim.setDuration(RESET_DURATION);
        anim.start();
    }

    private void animateTranslationX() {
        if (getCurrentDisplayedWidth() > getWidth()) {
            //the left edge is too far to the interior
            if (bounds.left > 0) {
                animateMatrixIndex(Matrix.MTRANS_X, 0);
            }
            //the right edge is too far to the interior
            else if (bounds.right < getWidth()) {
                animateMatrixIndex(Matrix.MTRANS_X, bounds.left + getWidth() - bounds.right);
            }
        } else {
            //left edge needs to be pulled in, and should be considered before the right edge
            if (bounds.left < 0) {
                animateMatrixIndex(Matrix.MTRANS_X, 0);
            }
            //right edge needs to be pulled in
            else if (bounds.right > getWidth()) {
                animateMatrixIndex(Matrix.MTRANS_X, bounds.left + getWidth() - bounds.right);
            }
        }
    }

    private void animateTranslationY() {
        if (getCurrentDisplayedHeight() > getHeight()) {
            //the top edge is too far to the interior
            if (bounds.top > 0) {
                animateMatrixIndex(Matrix.MTRANS_Y, 0);
            }
            //the bottom edge is too far to the interior
            else if (bounds.bottom < getHeight()) {
                animateMatrixIndex(Matrix.MTRANS_Y, bounds.top + getHeight() - bounds.bottom);
            }
        } else {
            //top needs to be pulled in, and needs to be considered before the bottom edge
            if (bounds.top < 0) {
                animateMatrixIndex(Matrix.MTRANS_Y, 0);
            }
            //bottom edge needs to be pulled in
            else if (bounds.bottom > getHeight()) {
                animateMatrixIndex(Matrix.MTRANS_Y, bounds.top + getHeight() - bounds.bottom);
            }
        }
    }

    private void animateMatrixIndex(final int index, final float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(matrixValues[index], to);
        animator.addUpdateListener(new AnimatorUpdateListener() {

            final float[] values = new float[9];
            Matrix current = new Matrix();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                current.set(getImageMatrix());
                current.getValues(values);
                values[index] = (Float) animation.getAnimatedValue();
                current.setValues(values);
                setImageMatrix(current);
            }
        });
        animator.setDuration(RESET_DURATION);
        animator.start();
    }

    /**
     * Get the x distance to translate the current image.
     *
     * @param toX   the current x location of touch focus
     * @param fromX the last x location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private float getXDistance(final float toX, final float fromX) {
        float xdistance = toX - fromX;

        if (restrictBounds) {
            xdistance = getRestrictedXDistance(xdistance);
        }

        //prevents image from translating an infinite distance offscreen
        if (bounds.right + xdistance < 0) {
            xdistance = -bounds.right;
        }
        else if (bounds.left + xdistance > getWidth()) {
            xdistance = getWidth() - bounds.left;
        }

        return xdistance;
    }

    /**
     * Get the horizontal distance to translate the current image, but restrict
     * it to the outer bounds of the {@link ImageView}. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     * @param xdistance the current desired horizontal distance to translate
     * @return the actual horizontal distance to translate with bounds restrictions
     */
    private float getRestrictedXDistance(final float xdistance) {
        float restrictedXDistance = xdistance;

        if (getCurrentDisplayedWidth() >= getWidth()) {
            if (bounds.left <= 0 && bounds.left + xdistance > 0 && !scaleDetector.isInProgress()) {
                restrictedXDistance = -bounds.left;
            } else if (bounds.right >= getWidth() && bounds.right + xdistance < getWidth() && !scaleDetector.isInProgress()) {
                restrictedXDistance = getWidth() - bounds.right;
            }
        } else if (!scaleDetector.isInProgress()) {
            if (bounds.left >= 0 && bounds.left + xdistance < 0) {
                restrictedXDistance = -bounds.left;
            } else if (bounds.right <= getWidth() && bounds.right + xdistance > getWidth()) {
                restrictedXDistance = getWidth() - bounds.right;
            }
        }

        return restrictedXDistance;
    }

    /**
     * Get the y distance to translate the current image.
     *
     * @param toY   the current y location of touch focus
     * @param fromY the last y location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private float getYDistance(final float toY, final float fromY) {
        float ydistance = toY - fromY;

        if (restrictBounds) {
            ydistance = getRestrictedYDistance(ydistance);
        }

        //prevents image from translating an infinite distance offscreen
        if (bounds.bottom + ydistance < 0) {
            ydistance = -bounds.bottom;
        }
        else if (bounds.top + ydistance > getHeight()) {
            ydistance = getHeight() - bounds.top;
        }

        return ydistance;
    }

    /**
     * Get the vertical distance to translate the current image, but restrict
     * it to the outer bounds of the {@link ImageView}. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     * @param ydistance the current desired vertical distance to translate
     * @return the actual vertical distance to translate with bounds restrictions
     */
    private float getRestrictedYDistance(final float ydistance) {
        float restrictedYDistance = ydistance;

        if (getCurrentDisplayedHeight() >= getHeight()) {
            if (bounds.top <= 0 && bounds.top + ydistance > 0 && !scaleDetector.isInProgress()) {
                restrictedYDistance = -bounds.top;
            } else if (bounds.bottom >= getHeight() && bounds.bottom + ydistance < getHeight() && !scaleDetector.isInProgress()) {
                restrictedYDistance = getHeight() - bounds.bottom;
            }
        } else if (!scaleDetector.isInProgress()) {
            if (bounds.top >= 0 && bounds.top + ydistance < 0) {
                restrictedYDistance = -bounds.top;
            } else if (bounds.bottom <= getHeight() && bounds.bottom + ydistance > getHeight()) {
                restrictedYDistance = getHeight() - bounds.bottom;
            }
        }

        return restrictedYDistance;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        //calculate value we should scale by, ultimately the scale will be startScale*scaleFactor
        scaleBy = (startScale * detector.getScaleFactor()) / matrixValues[Matrix.MSCALE_X];

        //what the scaling should end up at after the transformation
        final float projectedScale = scaleBy * matrixValues[Matrix.MSCALE_X];

        //clamp to the min/max if it's going over
        if (projectedScale < calculatedMinScale) {
            scaleBy = calculatedMinScale / matrixValues[Matrix.MSCALE_X];
        } else if (projectedScale > calculatedMaxScale) {
            scaleBy = calculatedMaxScale / matrixValues[Matrix.MSCALE_X];
        }

        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        startScale = matrixValues[Matrix.MSCALE_X];
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        scaleBy = 1f;
    }
}
