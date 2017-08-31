package com.legendmohe.circleview;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.legendmohe.circleview.model.Dot;
import com.legendmohe.circleview.model.Oval;

import java.util.Random;

/**
 * 放小米空气净化器吸附粒子背景动画
 */
public class AdsorbView extends View {
    private static final String TAG = "CircleView";

    private Paint mOvalPaint;
    private Paint mDotPaint;
    private @ColorInt
    int mPaintColor;

    private ValueAnimator mDotMoveAnimator;
    private Random mRandom = new Random();
    private AnimatorSet mAnimatorSet;

    private Dot[] mDots;
    private int mCenterX, mCenterY;

    private int mNumOfDot = 30;
    private int mDotMovingDuration = 1000;
    private int mDotMaxVelocity = 2;
    private int mDotMinVelocity = 1;
    private int mDotMaxRadius = 8;
    private int mDotMinRadius = 2;
    private int mDotMinAlpha = 100;
    private int mDotMaxAplha = 255;
    private int mDotBornMargin = 30;

    public AdsorbView(Context context) {
        super(context);
        setup();
    }

    public AdsorbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public AdsorbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AdsorbView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    private void setup() {
        mPaintColor = Color.WHITE;

        mDotPaint = new Paint();
        mDotPaint.setColor(mPaintColor);
        mDotPaint.setAntiAlias(true);
        mDotPaint.setStyle(Paint.Style.FILL);

        mDotMoveAnimator = new ValueAnimator();
        mDotMoveAnimator.setInterpolator(new LinearInterpolator());
        mDotMoveAnimator.setDuration(mDotMovingDuration);
        mDotMoveAnimator.setIntValues(0, mDotMovingDuration);
        mDotMoveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0; i < mNumOfDot; i++) {
                    Dot dot = mDots[i];
                    dot.distance -= dot.velocity;
                    dot.alpha = (int) (dot.baseAlpha*((double)(dot.distance)/(dot.baseDistance)));
                    // 中心空出100的半径范围
                    if (dot.distance <= 100) {
                        randomDot(dot);
                    }
                }
                invalidate();
            }
        });
        mDotMoveAnimator.setRepeatMode(ValueAnimator.RESTART);
        mDotMoveAnimator.setRepeatCount(ValueAnimator.INFINITE);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(mDotMoveAnimator);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCenterX = w/2;
        mCenterY = h/2;
        setupDrawables(w, h);
        if (mAnimatorSet.isStarted()) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet.start();
    }

    private void setupDrawables(int w, int h) {
        mDots = new Dot[mNumOfDot];
        for (int i = 0; i < mNumOfDot; i++) {
            mDots[i] = new Dot(mCenterX, mCenterY);
            randomDot(mDots[i]);
        }
    }

    private void randomDot(Dot dot) {
        int w = getWidth();
        int h = getHeight();
        int maxSize = w>=h?w:h;

        int minDistance =200 + mDotBornMargin;
        int alpha = mRandom.nextInt(mDotMaxAplha - mDotMinAlpha) + mDotMinAlpha;
        double angle = Math.toRadians(mRandom.nextInt(360));
        int distance = mRandom.nextInt((int) Math.sqrt(maxSize/2.0*maxSize/2.0 + maxSize/2.0*maxSize/2.0) - minDistance) + minDistance;
        int velocity = mRandom.nextInt(mDotMaxVelocity - mDotMinVelocity) + mDotMinVelocity;
        int radius = mRandom.nextInt(mDotMaxRadius - mDotMinRadius) + mDotMinRadius;

        dot.setAlpha(alpha);
        dot.setBaseAlpha(alpha);
        dot.setAngle(angle);
        dot.setDistance(distance);
        dot.setBaseDistance(distance);
        dot.setVelocity(velocity);
        dot.setRadius(radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mNumOfDot; i++) {
            Dot dot = mDots[i];
            mDotPaint.setAlpha(dot.alpha);
            canvas.drawCircle(dot.getCenterX(), dot.getCenterY(), dot.radius, mDotPaint);
        }
    }

    public void setPaintColor(@ColorInt int color) {
        mPaintColor = color;
        mDotPaint.setColor(mPaintColor);
        mOvalPaint.setColor(mPaintColor);
    }

    public int getPaintColor() {
        return mPaintColor;
    }

    public void setNumOfDot(final int numOfDot) {
        postOnAnimation(new Runnable() {
            @Override
            public void run() {
                Dot[] copyDots = new Dot[numOfDot];
                System.arraycopy(mDots, 0, copyDots, 0, Math.min(mDots.length, numOfDot));

                int numberDelta = numOfDot - mNumOfDot;
                if (numberDelta > 0) {
                    for (int i = 0; i < numberDelta; i++) {
                        copyDots[i + mNumOfDot] = new Dot(mCenterX, mCenterY);
                        randomDot(copyDots[i + mNumOfDot]);
                    }
                }
                mNumOfDot = numOfDot;
                mDots = copyDots;
            }
        });
        postInvalidateOnAnimation();
    }

    public void setDotVelocity(final int maxVelocity, final int minVelocity) {
        postOnAnimation(new Runnable() {

            @Override
            public void run() {
                mDotMaxVelocity = maxVelocity;
                mDotMinVelocity = minVelocity;

                if (mDotMinVelocity <= 0) {
                    mDotMinVelocity = 1;
                }
                mDotMaxVelocity = Math.max(mDotMaxVelocity, mDotMinVelocity);
            }
        });
        postInvalidateOnAnimation();
    }
}
