/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.keyguard;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.ContextThemeWrapper;

import androidx.annotation.StyleRes;

import com.android.systemui.Interpolators;
import com.android.systemui.R;

/**
 * Provides background color and radius animations for key pad buttons.
 */
class NumPadAnimator {
    private AnimatorSet mAnimator;
    private ValueAnimator mExpandAnimator;
    private ValueAnimator mContractAnimator;
    private GradientDrawable mBackground;
    private RippleDrawable mRipple;
    private GradientDrawable mRippleMask;
    private int mNormalColor;
    private int mHighlightColor;
    private int mStyle;

    NumPadAnimator(Context context, LayerDrawable drawable, @StyleRes int style) {
        LayerDrawable ld = (LayerDrawable) drawable.mutate();
        mBackground = (GradientDrawable) ld.findDrawableByLayerId(R.id.background);
        mRipple = (RippleDrawable) ld.findDrawableByLayerId(R.id.ripple);
        mRippleMask = (GradientDrawable) mRipple.findDrawableByLayerId(android.R.id.mask);
        mStyle = style;

        reloadColors(context);

        // Actual values will be updated later, usually during an onLayout() call
        mAnimator = new AnimatorSet();
        mExpandAnimator = ValueAnimator.ofFloat(0f, 1f);
        mExpandAnimator.setDuration(50);
        mExpandAnimator.setInterpolator(Interpolators.LINEAR);
        mExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator anim) {
                    mBackground.setCornerRadius((float) anim.getAnimatedValue());
                    mRippleMask.setCornerRadius((float) anim.getAnimatedValue());
                }
        });

        mContractAnimator = ValueAnimator.ofFloat(1f, 0f);
        mContractAnimator.setStartDelay(33);
        mContractAnimator.setDuration(417);
        mContractAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        mContractAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator anim) {
                    mBackground.setCornerRadius((float) anim.getAnimatedValue());
                    mRippleMask.setCornerRadius((float) anim.getAnimatedValue());
                }
        });
        mAnimator.playSequentially(mExpandAnimator, mContractAnimator);
    }

    void onLayout(int height) {
        float startRadius = height / 2f;
        float endRadius = height / 4f;
        mBackground.setCornerRadius(startRadius);
        mExpandAnimator.setFloatValues(startRadius, endRadius);
        mContractAnimator.setFloatValues(endRadius, startRadius);
    }

    void start() {
        mAnimator.cancel();
        mAnimator.start();
    }

    /**
     * Reload colors from resources.
     **/
    void reloadColors(Context context) {
        int[] customAttrs = {android.R.attr.colorControlNormal,
                android.R.attr.colorControlHighlight};

        ContextThemeWrapper ctw = new ContextThemeWrapper(context, mStyle);
        TypedArray a = ctw.obtainStyledAttributes(customAttrs);
        mNormalColor = a.getColor(0, 0);
        mHighlightColor = a.getColor(1, 0);
        a.recycle();

        mBackground.setColor(mNormalColor);
        mRipple.setColor(ColorStateList.valueOf(mHighlightColor));
    }
}

