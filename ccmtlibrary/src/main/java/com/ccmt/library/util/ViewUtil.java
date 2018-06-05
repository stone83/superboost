package com.ccmt.library.util;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class ViewUtil {

    @SuppressWarnings("unused")
    public static float obtainViewPx(Context context, float viewPx,
                                     boolean width) {
        if (width) {
            return viewPx * ScreenUtils.getScreenWidth(context) / 720;
        } else {
            return viewPx * ScreenUtils.getScreenHeight(context) / 1280;
        }
    }

    public static int obtainViewPx(Context context, int viewPx, boolean
            width) {
        if (width) {
            return viewPx * ScreenUtils.getScreenWidth(context) / 720;
        } else {
            return viewPx * ScreenUtils.getScreenHeight(context) / 1280;
        }
    }

    public static void setVisibility(View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    public static void setText(TextView view, int resid) {
        String str = view.getResources().getString(resid);
        setText(view, str);
    }

    public static void setText(TextView view, String str) {
        if (!str.equals(view.getText().toString())) {
            view.setText(str);
        }
    }

    @SuppressWarnings("unused")
    public static void setSelected(View view, boolean select) {
        if (select) {
            if (!view.isSelected()) {
                view.setSelected(true);
            }
        } else {
            if (view.isSelected()) {
                view.setSelected(false);
            }
        }
    }

    public static void setEnable(View view, boolean enabled) {
        if (view != null) {
            if (view.isEnabled() != enabled) {
                view.setEnabled(enabled);
            }
        }
    }

    public static void viewPostInvalidateOnAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 16)
            view.postInvalidateOnAnimation();
        else
            view.invalidate();
    }

    public static void removeOnGlobalLayoutListener(
            View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {

        if (Build.VERSION.SDK_INT < 17) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }

    }
}