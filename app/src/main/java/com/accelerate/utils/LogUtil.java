package com.accelerate.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.accessibility.AccessibilityNodeInfo;

import com.accelerate.accessibility.notifycation.IStatusBarNotification;
import com.boost.booster.clean.BuildConfig;
import com.ccmt.library.util.ScreenUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Log记录类
 */
public class LogUtil {

    private static final boolean DEBUG = BuildConfig.IS_DEBUG;
    private static final String TAG = "MyLog";

    public static void i(String TAG, String msg) {
        if (DEBUG) {
            android.util.Log.i(TAG, "[" + getFileLineMethod() + "]" + msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            android.util.Log.i(TAG, msg);
        }
    }

    @SuppressWarnings("unused")
    public static void d(String TAG, String method, String msg) {
        android.util.Log.d(TAG, "[" + method + "]" + msg);
    }

    @SuppressWarnings("unused")
    public static void d(String TAG, String msg) {
        if (DEBUG) {
            android.util.Log.d(TAG, "[" + getFileLineMethod() + "]" + msg);
        }
    }

    @SuppressWarnings("unused")
    public static void d(String msg) {
        if (DEBUG) {
            android.util.Log.d(_FILE_(), "[" + getLineMethod() + "]" + msg);
        }
    }

    @SuppressWarnings("unused")
    public static void e(String msg) {
        if (DEBUG) {
            android.util.Log.e("UI", getFileLineMethod() + msg);
        }
    }

    @SuppressWarnings("unused")
    public static void e(String TAG, String msg, Exception e) {
        if (DEBUG) {
            android.util.Log.e(TAG, msg, e);
        }
    }

    @SuppressWarnings("unused")
    public static void e(String TAG, String msg) {
        if (DEBUG) {
            android.util.Log.e(TAG, getLineMethod() + msg);
        }
    }

    private static String getFileLineMethod() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return "[" +
                traceElement.getFileName() + " | " +
                traceElement.getLineNumber() + " | " +
                traceElement.getMethodName() + "]";
    }

    private static String getLineMethod() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return "[" +
                traceElement.getLineNumber() + " | " +
                traceElement.getMethodName() + "]";
    }

    private static String _FILE_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getFileName();
    }
//
//    public static String _FUNC_() {
//        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
//        return traceElement.getMethodName();
//    }
//
//    public static int _LINE_() {
//        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
//        return traceElement.getLineNumber();
//    }
//
//    public static String _TIME_() {
//        Date now = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",
//                Locale.getDefault());
//        return sdf.format(now);
//    }

    /**
     * 这是测试代码,做屏幕适配有时想看看当前手机的分辨率,调用该方法就可以.
     *
     * @param context
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static void showScreenWidthAndHeight(Context context) {
        i("ScreenUtils.getScreenWidth(context) -> " + ScreenUtils.getScreenWidth(context));
        i("ScreenUtils.getScreenHeight(context) -> " + ScreenUtils.getScreenHeight(context));
        i("ScreenUtils.getScreenHeightReal((Activity) context) -> " + ScreenUtils.getScreenHeightReal((Activity) context));
    }

    /**
     * 这是测试代码,显示相关文件路径.
     *
     * @param context
     */
    @SuppressWarnings({"JavaDoc", "unused", "WeakerAccess"})
    public static void showFilePath(Context context) {
        i("context.getApplicationInfo().nativeLibraryDir -> " + context.getApplicationInfo().nativeLibraryDir);
        i("context.getApplicationInfo().sourceDir -> " + context.getApplicationInfo().sourceDir);
        i("context.getApplicationInfo().publicSourceDir -> " + context.getApplicationInfo().publicSourceDir);
        i("context.getPackageCodePath() -> " + context.getPackageCodePath());
        i("context.getPackageResourcePath() -> " + context.getPackageResourcePath());
//        String appPathAtSystem = RootUtil.obtainAppPathAtSystem(context.getPackageName(), true);
//        String appPath = RootUtil.obtainAppPathAtSystem(context.getPackageName(), false);
//        i("appPathAtSystem -> " + appPathAtSystem);
//        i("appPath -> " + appPath);
//        if (appPathAtSystem != null) {
//            i("new File(appPathAtSystem).length() -> " + new File(appPathAtSystem).length());
//            i("new File(appPathAtSystem).exists() -> " + new File(appPathAtSystem).exists());
//        }
//        if (appPath != null) {
//            i("new File(appPath).length() -> " + new File(appPath).length());
//            i("new File(appPath).exists() -> " + new File(appPath).exists());
//        }
    }

    @SuppressWarnings("unused")
    public static void showMethodInfo(Object obj) {
        Class<?> cla = obj.getClass();
        Method[] ms = cla.getDeclaredMethods();
        for (Method m : ms) {
            i("ms[i].toString() -> " + m.toString());
        }
    }

    @SuppressWarnings("unused")
    public static void showFieldInfo(Object obj) {
        Class<?> cla = obj.getClass();
        Field[] fs = cla.getDeclaredFields();
        for (Field f : fs) {
            try {
                f.setAccessible(true);
                i(f + " -> " + f.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    public static void showCollectionInfo(Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            for (Object aCollection : collection) {
                i("aCollection -> " + aCollection);
            }
        } else if (obj instanceof Map) {
            Map map = (Map) obj;
            Map.Entry next;
            for (Object o : map.entrySet()) {
                next = (Map.Entry) o;
                i(next.getKey() + " -> " + next.getValue());
            }
        } else if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            Object value;
            for (int i = 0; i < length; i++) {
                value = Array.get(obj, i);
                i("value -> " + value);
            }
        }
    }

    /**
     * 根据指定包名显示app的相关信息
     *
     * @param context
     * @param packageName
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static void showApplicationInfo(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i("Arrays.toString(applicationInfo.splitSourceDirs) -> " + Arrays.toString(applicationInfo.splitSourceDirs));
            }
            i("applicationInfo.loadLabel(packageManager).toString() -> " + applicationInfo.loadLabel(packageManager).toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i("Arrays.toString(applicationInfo.splitPublicSourceDirs) -> " + Arrays.toString(applicationInfo.splitPublicSourceDirs));
            }
            i("applicationInfo.name -> " + applicationInfo.name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LogUtil.i("Arrays.toString(applicationInfo.splitSourceDirs)) -> " + Arrays.toString(applicationInfo.splitSourceDirs));
            }
            i("applicationInfo.publicSourceDir -> " + applicationInfo.publicSourceDir);
            i("applicationInfo.sourceDir -> " + applicationInfo.sourceDir);
            i("applicationInfo.backupAgentName -> " + applicationInfo.backupAgentName);
            i("applicationInfo.className -> " + applicationInfo.className);
            i("applicationInfo.dataDir -> " + applicationInfo.dataDir);
            i("applicationInfo.manageSpaceActivityName -> " + applicationInfo.manageSpaceActivityName);
            i("applicationInfo.nativeLibraryDir -> " + applicationInfo.nativeLibraryDir);
            i("applicationInfo.permission -> " + applicationInfo.permission);
            i("applicationInfo.processName -> " + applicationInfo.processName);
            i("applicationInfo.taskAffinity -> " + applicationInfo.taskAffinity);
            i("applicationInfo.compatibleWidthLimitDp -> " + applicationInfo.compatibleWidthLimitDp);
            i("applicationInfo.enabled -> " + applicationInfo.enabled);
            i("applicationInfo.loadDescription(packageManager) -> " + applicationInfo.loadDescription(packageManager));
            i("Arrays.toString(applicationInfo.sharedLibraryFiles) -> " + Arrays.toString(applicationInfo.sharedLibraryFiles));
            i("applicationInfo.targetSdkVersion -> " + applicationInfo.targetSdkVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HardwareIds")
    @SuppressWarnings({"unused", "deprecation"})
    public static void showPhoneInfo() {
        i("Build.MANUFACTURER -> " + Build.MANUFACTURER);
        i("Build.MODEL -> " + Build.MODEL);
        i("Build.BOARD -> " + Build.BOARD);
        i("Build.BOOTLOADER -> " + Build.BOOTLOADER);
        i("Build.DEVICE -> " + Build.DEVICE);
        i("Build.DISPLAY -> " + Build.DISPLAY);
        i("Build.FINGERPRINT -> " + Build.FINGERPRINT);
        i("Build.getRadioVersion() -> " + Build.getRadioVersion());
        i("Build.HARDWARE -> " + Build.HARDWARE);
        i("Build.HOST -> " + Build.HOST);
        i("Build.ID -> " + Build.ID);
        i("Build.PRODUCT -> " + Build.PRODUCT);
        i("Build.SERIAL -> " + Build.SERIAL);
        i("Build.TAGS -> " + Build.TAGS);
        i("Build.TYPE -> " + Build.TYPE);
        i("Build.UNKNOWN -> " + Build.UNKNOWN);
        i("Build.USER -> " + Build.USER);
        i("Build.CPU_ABI -> " + Build.CPU_ABI);
        i("Build.CPU_ABI2 -> " + Build.CPU_ABI2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            i("Arrays.toString(Build.SUPPORTED_32_BIT_ABIS) -> " + Arrays.toString(Build.SUPPORTED_32_BIT_ABIS));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            i("Arrays.toString(Build.SUPPORTED_64_BIT_ABIS) -> " + Arrays.toString(Build.SUPPORTED_64_BIT_ABIS));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            i("Arrays.toString(Build.SUPPORTED_ABIS) -> " + Arrays.toString(Build.SUPPORTED_ABIS));
        }
        i("Build.TIME -> " + Build.TIME);
    }

    @SuppressWarnings("unused")
    public static void showExternalStorageInfo() {
        File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + Environment.DIRECTORY_DOWNLOADS);
        File downloadDir2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        i("downloadDir.getAbsolutePath() -> " + downloadDir.getAbsolutePath());
        i("downloadDir2.getAbsolutePath() -> " + downloadDir2.getAbsolutePath());
        i("downloadDir.exists() -> " + downloadDir.exists());
        i("downloadDir2.exists() -> " + downloadDir2.exists());
        i("Environment.getExternalStorageState() -> " + Environment.getExternalStorageState());
        String externalStorageState = null;
        String externalStorageState2 = null;
        if (Build.VERSION.SDK_INT >= 21) {
            externalStorageState = Environment.getExternalStorageState(downloadDir);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            externalStorageState2 = Environment.getExternalStorageState(downloadDir2);
        }
        i("externalStorageState -> " + externalStorageState);
        i("externalStorageState2 -> " + externalStorageState2);
    }

    @SuppressWarnings("unused")
    public static void showMemeryInfo(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        LogUtil.i("mi.availMem -> " + mi.availMem);
        if (Build.VERSION.SDK_INT >= 16) {
            LogUtil.i("mi.totalMem -> " + mi.totalMem);
        }
        LogUtil.i("am.getMemoryClass() -> " + am.getMemoryClass());
        LogUtil.i("am.getLargeMemoryClass() -> " + am.getLargeMemoryClass());
        Runtime runtime = Runtime.getRuntime();
        LogUtil.i("runtime.maxMemory() -> " + runtime.maxMemory());
        LogUtil.i("runtime.totalMemory() -> " + runtime.totalMemory());
        LogUtil.i("runtime.freeMemory() -> " + runtime.freeMemory());
    }

    public static void showStatusBarNotificationInfo(IStatusBarNotification sbn) {
        Bundle extras;
        String notificationTitle = null;
        Bitmap notificationLargeIcon = null;
        Bitmap notificationSmallIcon = null;
        CharSequence notificationText = null;
        CharSequence notificationSubText = null;
        if (Build.VERSION.SDK_INT >= 19) {
            extras = sbn.getNotification().extras;
            if (extras != null) {
                notificationTitle = extras.getString(Notification.EXTRA_TITLE);
                notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
                notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
                notificationLargeIcon = extras.getParcelable(Notification.EXTRA_LARGE_ICON);
                notificationSmallIcon = extras.getParcelable(Notification.EXTRA_SMALL_ICON);
            }
        }
        LogUtil.i("notificationTitle -> " + notificationTitle);
        LogUtil.i("notificationText -> " + notificationText);
        LogUtil.i("notificationSubText -> " + notificationSubText);
        LogUtil.i("(notificationLargeIcon != null) -> " + (notificationLargeIcon != null));
        LogUtil.i("(notificationSmallIcon != null) -> " + (notificationSmallIcon != null));
    }

    public static void showNodeInfo(AccessibilityNodeInfo node) {
        String text;
        AccessibilityNodeInfo accessibilityNodeInfo;
        CharSequence temp;
        if (node != null) {
            LogUtil.i("node.getClassName().toString() -> " + node.getClassName().toString());
            LogUtil.i("node.getText() -> " + node.getText());
            LogUtil.i("node.getContentDescription() -> " + node.getContentDescription());
            int childCount = node.getChildCount();
            LogUtil.i("node子节点的数量 -> " + childCount);
            for (int i = 0; i < childCount; i++) {
                accessibilityNodeInfo = node.getChild(i);
                LogUtil.i("------");
                LogUtil.i("node子节点的className -> " + accessibilityNodeInfo.getClassName().toString());
                temp = accessibilityNodeInfo.getText();
                if (temp != null) {
                    text = temp.toString();
                    LogUtil.i("node子节点的text -> " + text);
                }
                temp = accessibilityNodeInfo.getContentDescription();
                if (temp != null) {
                    text = temp.toString();
                    LogUtil.i("node子节点的contentDescription -> " + text);
                }
            }
        }
    }

}
