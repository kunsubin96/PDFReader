package com.kunsubin.pdfreader;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

public class AnimationUtils {
    public static Animation getFlashAnimation() {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }
}
