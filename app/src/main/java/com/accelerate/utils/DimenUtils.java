package com.accelerate.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

@SuppressWarnings("WeakerAccess")
public class DimenUtils {

    private static DisplayMetrics mMetrics = null;

    private static final int DP_TO_PX = TypedValue.COMPLEX_UNIT_DIP;
    private static final int SP_TO_PX = TypedValue.COMPLEX_UNIT_SP;
    private static final int PX_TO_DP = TypedValue.COMPLEX_UNIT_MM + 1;
    private static final int PX_TO_SP = TypedValue.COMPLEX_UNIT_MM + 2;
    private static final int DP_TO_PX_SCALE_H = TypedValue.COMPLEX_UNIT_MM + 3;
    private static final int DP_SCALE_H = TypedValue.COMPLEX_UNIT_MM + 4;
    private static final int DP_TO_PX_SCALE_W = TypedValue.COMPLEX_UNIT_MM + 5;

    private final static float BASE_SCREEN_WIDH = 720f;
    private final static float BASE_SCREEN_HEIGHT = 1280f;
    private final static float BASE_SCREEN_DENSITY = 2f;
    private static Float sScaleW, sScaleH;
    private static int sNavigationHeight = Integer.MIN_VALUE;

    private static DisplayMetrics getDisplayMetrics(Context context) {
        if (mMetrics != null) {
            return mMetrics;
        }
        if (context != null) {
            Resources res = context.getResources();
            if (res != null) {
                mMetrics = res.getDisplayMetrics();
                if (mMetrics != null) {
                    return mMetrics;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    private static float getDensity(Context context) {
        if (getDisplayMetrics(context) != null) {
            return getDisplayMetrics(context).density;
        } else {
            return 1.5f;
        }
    }

    /**
     * 如果要计算的值已经经过dip计算，则使用此结果，如果没有请使用getScaleFactorWithoutDip
     */
    private static float getScaleFactorW(Context context) {
        if (sScaleW == null) {
            sScaleW = (getScreenWidth(context) * BASE_SCREEN_DENSITY) / (getDensity(context) * BASE_SCREEN_WIDH);
        }
        return sScaleW;
    }

    private static float getScaleFactorH(Context context) {
        if (sScaleH == null) {
            sScaleH = (getScreenHeight(context) * BASE_SCREEN_DENSITY)
                    / (getDensity(context) * BASE_SCREEN_HEIGHT);
        }
        return sScaleH;
    }

    private static int getScreenWidth(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        if (metrics == null) {
            return 0;
        }
        return metrics.widthPixels;
    }

    private static int getScreenHeight(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        if (metrics == null) {
            return 0;
        }
        return metrics.heightPixels;
    }

    private static float applyDimension(Context context, int unit, float value, DisplayMetrics metrics) {
        if (metrics == null) {
            return 0;
        }
        switch (unit) {
            case DP_TO_PX:
            case SP_TO_PX:
                return TypedValue.applyDimension(unit, value, metrics);
            case PX_TO_DP:
                return value / metrics.density;
            case PX_TO_SP:
                return value / metrics.scaledDensity;
            case DP_TO_PX_SCALE_H:
                return TypedValue.applyDimension(DP_TO_PX, value * getScaleFactorH(context), metrics);
            case DP_SCALE_H:
                return value * getScaleFactorH(context);
            case DP_TO_PX_SCALE_W:
                return TypedValue.applyDimension(DP_TO_PX, value * getScaleFactorW(context), metrics);
        }
        return 0;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    @SuppressWarnings("unused")
    public static int dp2px(Context context, float dpValue) {
        return (int) applyDimension(context, DP_TO_PX, dpValue, getDisplayMetrics(context));
    }

    @SuppressWarnings("WeakerAccess")
    public static int getNavigationHeight(Context context) {
        if (sNavigationHeight == Integer.MIN_VALUE) {
            if (!isExceptProcessNavigationBar()) {
                synchronized (DimenUtils.class) {
                    sNavigationHeight = getNavigationHeightFromResource(context);
                }
            } else {
                sNavigationHeight = 0;
            }
        }
        return sNavigationHeight;
    }

    private static boolean isExceptProcessNavigationBar() {
        String deviceModel = SystemUtil.getDeviceModel();
        if (!TextUtils.isEmpty(deviceModel)) {
            if (deviceModel.equals("ZTE U950") || deviceModel.equals("ZTE U817") || deviceModel.equals("ZTE V955")
                    || deviceModel.equals("GT-S5301L")
                    || deviceModel.equals("LG-E425f") || deviceModel.equals("GT-S5303B")
                    || deviceModel.equals("I-STYLE2.1") || deviceModel.equals("SCH-S738C")
                    || deviceModel.equals("S120 LOIN") || deviceModel.equals("START 765")
                    || deviceModel.equals("LG-E425j") || deviceModel.equals("Archos 50 Titanium")
                    || deviceModel.equals("ZTE N880G") || deviceModel.equals("O+ 8.91")
                    || deviceModel.equals("ZP330") || deviceModel.equals("Wise+")
                    || deviceModel.equals("HUAWEI Y511-U30") || deviceModel.equals("Che1-L04")
                    || deviceModel.equals("ASUS_T00I") || deviceModel.equals("Lenovo A319")
                    || deviceModel.equals("Bird 72_wet_a_jb3") || deviceModel.equals("Sendtel Wise")
                    || deviceModel.equals("cross92_3923") || deviceModel.equals("HTC X920e")
                    || deviceModel.equals("ONE TOUCH 4033X") || deviceModel.equals("GSmart Roma")
                    || deviceModel.equals("A74B") || deviceModel.equals("Doogee Y100 Pro")
                    || deviceModel.equals("M4 SS1050") || deviceModel.equals("Ibiza_F2")
                    || deviceModel.equals("Lenovo P70-A") || deviceModel.equals("Y635-L21")
                    || deviceModel.equals("hi6210sft") || deviceModel.equals("TurboX6Z")
                    || deviceModel.equals("ONE TOUCH 4015A") || deviceModel.equals("LENNY2")
                    || deviceModel.equals("A66A*") || deviceModel.equals("ONE TOUCH 4033X")
                    || deviceModel.equals("LENNY2") || deviceModel.equals("PGN606")
                    || deviceModel.equals("MEU AN400") || deviceModel.equals("ONE TOUCH 4015X")
                    || deviceModel.equals("4013M") || deviceModel.equals("HUAWEI MT1-T00")
                    || deviceModel.equals("CHM-UL00")) {
                return true;
            }
        }
        return "OPPO".equals(Build.MANUFACTURER) || "Meizu".equals(Build.MANUFACTURER);
    }

    private static int getNavigationHeightFromResource(Context context) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int navigationBarHeight = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("config_showNavigationBar",
                "bool", "android");
        if (resourceId > 0) {
            boolean hasNav = resources.getBoolean(resourceId);
            if (hasNav) {
                resourceId = resources.getIdentifier("navigation_bar_height",
                        "dimen", "android");
                if (resourceId > 0) {
                    navigationBarHeight = resources
                            .getDimensionPixelSize(resourceId);
                }
            }
        }

        if (navigationBarHeight <= 0) {
            DisplayMetrics dMetrics = new DisplayMetrics();
            display.getMetrics(dMetrics);
            int screenHeight = Math.max(dMetrics.widthPixels, dMetrics.heightPixels);
            int realHeight = 0;
            try {
                Method mt = display.getClass().getMethod("getRealSize", Point.class);
                Point size = new Point();
                mt.invoke(display, size);
                realHeight = Math.max(size.x, size.y);
            } catch (NoSuchMethodException e) {
                Method mt = null;
                try {
                    mt = display.getClass().getMethod("getRawHeight");
                } catch (NoSuchMethodException e2) {
                    try {
                        mt = display.getClass().getMethod("getRealHeight");
                    } catch (NoSuchMethodException ignored) {

                    }
                }
                if (mt != null) {
                    try {
                        realHeight = (int) mt.invoke(display);
                    } catch (Exception ignored) {

                    }
                }
            } catch (Exception ignored) {

            }
            // 如果是橫屏,这种计算方式是不是会有问题.
            navigationBarHeight = realHeight - screenHeight;
        }

        return navigationBarHeight;
    }

}
