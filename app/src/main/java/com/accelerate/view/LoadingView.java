package com.accelerate.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

import com.ccmt.library.util.ViewUtil;

public class LoadingView extends View {

    private static final int TICK_COUNT = 12;
    private static final float TICK_HIGHT_FOCTOR = 8 / 25.0f;
    private static final float TICK_WIDTH_FOCTOR = 2 / 25.0f;
    private static final int ANIMATOR_DURATION = 800;

    private int mRadio = 100;
    private int mTickHight;
    @SuppressWarnings("FieldCanBeLocal")
    private int mTickWidth;
    private int mLightTick = 0;
    private int[] mAlphas;

    private PointF mCenterPoint;
    private Paint mTickPaint;

    private ValueAnimator valueAnimator;
    private TimeInterpolator interpolator = new LinearInterpolator();

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initValue();
                ViewUtil.removeOnGlobalLayoutListener(LoadingView.this, this);
            }
        });
    }

    private void initValue() {
        mTickPaint = new Paint();
        mTickPaint.setAntiAlias(true);
        mTickPaint.setStyle(Paint.Style.FILL);
        mTickPaint.setDither(true);
        mTickPaint.setColor(Color.WHITE);

        mAlphas = new int[TICK_COUNT];
        int foctor = (255 - 10) / TICK_COUNT;
        for (int i = 0; i < TICK_COUNT; i++) {
            mAlphas[i] = 255 - (i + 1) * foctor;
        }
        mRadio = (int) (Math.min(getWidth(), getHeight()) / (2.0f * (1 + TICK_HIGHT_FOCTOR)));
        mTickHight = (int) (mRadio * TICK_HIGHT_FOCTOR);
        mTickWidth = (int) (mRadio * TICK_WIDTH_FOCTOR);
        mTickPaint.setStrokeWidth(mTickWidth);
        mCenterPoint = new PointF(getWidth() / 2.0f, getHeight() / 2.0f);

        startAnimator();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mCenterPoint.x, mCenterPoint.y);
        drawProgress(canvas);
    }

    private void drawProgress(Canvas canvas) {
        canvas.save(); //记录画布状态
        canvas.rotate((360 / 2), 0, 0);
        float rAngle = -360 / (TICK_COUNT * 1.0f);
        for (int index = 0; index < TICK_COUNT; index++) {
            canvas.rotate(rAngle, 0, 0);
            mTickPaint.setAlpha(mAlphas[(index + mLightTick) % TICK_COUNT]);
            canvas.drawLine(0, -mRadio - mTickHight, 0, -mRadio, mTickPaint);
        }
        canvas.restore();
    }


    public void startAnimator() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator = ValueAnimator.ofInt(0, TICK_COUNT).setDuration(ANIMATOR_DURATION);
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(animation -> {
            mLightTick = (int) animation.getAnimatedValue();
            invalidate();
        });
        valueAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

}
