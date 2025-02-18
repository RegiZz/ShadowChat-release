package com.shadowchat.utils;


import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Context;

import com.shadowchat.R;

public class AnimationUtilities {

    public static void fadeIn(Context context, View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        view.startAnimation(fadeIn);
    }

    public static void slideInLeft(Context context, View view) {
        Animation slideInLeft = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
        view.startAnimation(slideInLeft);
    }

    public static void slideInRight(Context context, View view) {
        Animation slideInRight = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        view.startAnimation(slideInRight);
    }
}
