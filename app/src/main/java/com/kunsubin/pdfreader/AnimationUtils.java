package com.kunsubin.pdfreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import com.kunsubin.pdfreader.photoview.PhotoView;

public class AnimationUtils {
    public static Animation getFlashAnimation() {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }
    
    public static void imageViewAnimatedChange(Context context, final PhotoView photoView, final Bitmap bitmap) {
        final Animation anim_out = android.view.animation.AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        anim_out.setDuration(200);
        final Animation anim_in  = android.view.animation.AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        anim_in.setDuration(200);
        anim_out.setAnimationListener(new AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                photoView.setImageBitmap(bitmap);
                anim_in.setAnimationListener(new AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                photoView.startAnimation(anim_in);
            }
        });
        photoView.startAnimation(anim_out);
    }
}
