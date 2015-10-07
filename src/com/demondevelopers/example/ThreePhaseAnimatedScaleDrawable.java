package com.demondevelopers.example;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Created by Doorag on 15/9/5.
 * A 3-Phased Animation Implementation
 */
public class ThreePhaseAnimatedScaleDrawable extends AnimatedScaleDrawable {
    private int animStep = 1;
    private boolean hasMidScale = false;
    private final Rect mTmpRect = new Rect();

    public ThreePhaseAnimatedScaleDrawable()
    {
        super();
    }

    public ThreePhaseAnimatedScaleDrawable(Drawable drawable)
    {
        super(drawable);
    }

    public void setMidScale(float midScale)
    {
        mState.mMidScale = midScale;
    }

    public float getMidScale()
    {
        return mState.mMidScale;
    }

    @Override
    public void start()
    {
        if(mState.mAnimating){
            return;
        }

        if(mState.mInterpolator == null){
            mState.mInterpolator = new LinearInterpolator();
        }

        if(mState.mTransformation == null){
            mState.mTransformation = new Transformation();
        }
        else{
            mState.mTransformation.clear();
        }

        if(mState.mAnimation == null){
            mState.mAnimation = new AlphaAnimation(0.0f, 1.0f);
        }
        else{
            mState.mAnimation.reset();
        }
        //setup for 3-phased animation
        if(mState.mMidScale != 0){
            mState.mAnimation.setDuration(mState.mDuration / 3);
            mState.mAnimation.setRepeatMode(Animation.RESTART);
            mState.mAnimation.setRepeatCount(2);
            hasMidScale = true;
        }
        mState.mAnimation.setDuration(mState.mDuration);
        mState.mAnimation.setInterpolator(mState.mInterpolator);
        mState.mAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
        mState.mAnimating = true;

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas)
    {
        final AnimationScaleState st = mState;

        if(st.mDrawable == null){
            return;
        }

        final Rect bounds = (st.mUseBounds ? getBounds() : mTmpRect);

        int saveCount = canvas.save();
        canvas.scale(st.mScale, st.mScale,
                bounds.left + bounds.width()  / 2,
                bounds.top  + bounds.height() / 2);
        st.mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);

        if (st.mAnimating) {
            long animTime = AnimationUtils.currentAnimationTimeMillis();
            st.mAnimation.getTransformation(animTime, st.mTransformation);
            float transformation = st.mTransformation.getAlpha();
            if (!hasMidScale) {
                st.mScale = (st.mMinScale
                        + (st.mMaxScale - st.mMinScale) * (st.mInvert ? (1.0f - transformation) : transformation));

                invalidateSelf();
                if (transformation == 1.0f)
                    stop();
            }
            // An implementation of 3-Phased Animation using AlphaAnimation
            else {
                if (animStep == 1) {
                    st.mScale = (st.mMidScale
                            + (st.mMaxScale - st.mMidScale) * (st.mInvert ? (1.0f - transformation) : transformation));
                    if (transformation == 1.0f) {
                        animStep = 2;
                    }
                } else if (animStep == 2) {
                    st.mScale = (st.mMaxScale
                            - (st.mMaxScale - st.mMinScale) * (st.mInvert ? (1.0f - transformation) : transformation));
                    if (transformation == 1.0f){
                        animStep = 3;
                    }
                } else if (animStep == 3) {
                    st.mScale = (st.mMinScale
                            + (st.mMidScale - st.mMinScale) * (st.mInvert ? (1.0f - transformation) : transformation));
                    if (transformation == 1.0f) {
                        animStep = 1;
                        stop();
                    }
                }
                invalidateSelf();
            }

        }

    }



}
