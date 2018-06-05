package com.ccmt.library.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class ScreenUtils {

    private ScreenUtils() {
        // cannot be instantiated
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 返回当前屏幕是否为竖屏。
     *
     * @param context
     * @return 当且仅当当前屏幕为竖屏时返回true, 否则返回false。
     */
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == //
                Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 获取不包括状态栏和标题栏的屏幕高度
     *
     * @param activity
     * @return
     */
    public static int getScreenHeightReal(Activity activity) {
        return getContentView(activity).getHeight();
    }

    /**
     * 获取标题栏高度
     *
     * @param activity
     * @return
     */
    public static int getTitleBarHeight(Activity activity) {
        int contentTop = getContentView(activity).getTop();
        if (contentTop == 0) {
            return 0;
        }
        return contentTop - getStatusHeight(activity);
    }

    /**
     * 获取状态栏的高度,getWindowVisibleDisplayFrame()方法拿到包括标题栏但不包括状态栏的区域,必须界面完全显示才可以拿到,
     * 属于动态行为.
     *
     * @param activity
     * @return
     */
    public static int getStatusHeight(Activity activity) {
        int contentTop = getContentView(activity).getTop();
        if (contentTop == 0) {
            return 0;
        }
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 获取状态栏的高度,不管界面是不是有状态栏,还是隐藏了状态栏,都能拿到状态栏的高度,
     * <p>
     * 因为是反射系统dimen资源的内部类的1个静态常量,属于静态行为.
     *
     * @param context
     * @return
     */
    public static int getStatusHeight2(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            // Object object = clazz.newInstance();
            // Field f = clazz.getField("status_bar_height");
            // Log.i("MyLog", "f.getName() -> " + f.getName());
            // Log.i("MyLog", "f.toString() -> " + f.toString());
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(null).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static FrameLayout getContentView(Activity activity) {
        return (FrameLayout) activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }

}
