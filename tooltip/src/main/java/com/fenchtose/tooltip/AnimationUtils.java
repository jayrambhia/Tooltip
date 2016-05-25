package com.fenchtose.tooltip;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by Jay Rambhia on 5/25/16.
 */
public class AnimationUtils {

    @NonNull
    public static Animator fade(@NonNull final View view, float fromAlpha, float toAlpha, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", fromAlpha, toAlpha);
        animator.setDuration(duration);
        return animator;
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator reveal(@NonNull final View view, int cx, int cy, int startRadius, int finalRadius, int duration) {
        Animator animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius, finalRadius);
        animator.setDuration(duration);
        return animator;
    }

    @NonNull
    public static Animator slideY(@NonNull View view, int fromY, int toY, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "y", toY, fromY, toY);
        animator.setDuration(duration);
        return animator;
    }

    @NonNull
    public static Animator scaleY(@NonNull View view, int pivotX, int pivotY, float fromScale, float toScale, int duration) {
        view.setPivotX(pivotX);
        view.setPivotY(pivotY);
        Animator animator = ObjectAnimator.ofFloat(view, "scaleY", fromScale, toScale);
        animator.setDuration(duration);
        return animator;
    }

    @NonNull
    public static Animator scaleX(@NonNull View view, int pivotX, int pivotY, float fromScale, float toScale, int duration) {
        view.setPivotX(pivotX);
        view.setPivotY(pivotY);
        Animator animator = ObjectAnimator.ofFloat(view, "scaleX", fromScale, toScale);
        animator.setDuration(duration);
        return animator;
    }

}
