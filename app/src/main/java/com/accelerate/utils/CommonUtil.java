package com.accelerate.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.support.v4.util.ArrayMap;

import com.accelerate.domain.ProcessInfo;
import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {
    public static <T> List<T> listInit(List<T> list) {
        if (list == null) {
            list = new ArrayList<>();
        } else {
            if (list.size() > 0) {
                list.clear();
            }
        }
        return list;
    }

    public static <T> void listAdd(List<T> list, T t) {
        if (!list.contains(t)) {
            list.add(t);
        }
    }

    public static <T> void listRemove(List<T> list, T t) {
        if (list.contains(t)) {
            list.remove(t);
        }
    }

    @SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
    public static Activity obtainTopActivity() {
        Activity topActivity = null;
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method getATMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            Object activityThread = getATMethod.invoke(null);
            activitiesField.setAccessible(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ArrayMap activites = (ArrayMap) activitiesField.get(activityThread);
                if (activites == null || activites.size() == 0) {
                    return null;
                }
                Object activityClientRecord = activites.valueAt(0);

                Class activityClientRecordClass = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                topActivity = (Activity) activityField.get(activityClientRecord);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
//        LogUtil.i("topActivity -> " + topActivity);
        return topActivity;
    }

    /**
     * 获取指定包名的app的进程信息
     *
     * @param context
     * @param packageName
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess", "deprecation"})
    public static List<ProcessInfo> obtainCurrentProcessInfo(Context context, String[] packageNames) {
        List<ProcessInfo> result;
        ProcessInfo processInfo;
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 21) {
            List<AndroidAppProcess> androidAppProcesses = ProcessManager.getRunningAppProcesses();
            if (androidAppProcesses == null) {
                return null;
            }
            AndroidAppProcess androidAppProcess;
            int size = androidAppProcesses.size();
            if (size == 0) {
                return null;
            }
            String packageNameTemp;
            result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                androidAppProcess = androidAppProcesses.get(i);
                if (androidAppProcess == null) {
                    continue;
                }

                packageNameTemp = androidAppProcess.getPackageName();
                if (packageNameTemp == null) {
                    continue;
                }

                ApplicationInfo applicationInfo;
                CharSequence applicationLabel;
                try {
                    applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                    applicationLabel = applicationInfo.loadLabel(packageManager);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{androidAppProcess.pid});

                processInfo = new ProcessInfo();
                if (packageNames.length != 0) {
                    for (String packageName:packageNames) {
                        if (!packageNameTemp.equals(packageName)) {
//                        LogUtil.i("------");
//                        LogUtil.i("packageNameTemp -> " + packageNameTemp);
//                        LogUtil.i("androidAppProcess.name -> " + androidAppProcess.name);
//                        LogUtil.i("applicationLabel -> " + applicationLabel);
//                        LogUtil.i("isRunningApp -> " + isRunningApp(context, packageNameTemp));

                            String lowerCase = applicationLabel.toString().toLowerCase();
                            if (packageNameTemp.contains("setting") || lowerCase.contains("android系统")
                                    || lowerCase.contains("android 系统")) {
                                continue;
                            }

                            processInfo.setPackageName(packageNameTemp);
                            processInfo.setProcessName(androidAppProcess.name);
                            processInfo.setPid(androidAppProcess.pid);
                            processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                            processInfo.setAppName(applicationLabel.toString());
                            processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
                            processInfo.setSystemApp(false);
                            if (!result.contains(processInfo)) {
                                result.add(processInfo);
                            }
                        }
                    }
                } else {
                    String lowerCase = applicationLabel.toString().toLowerCase();
                    if (packageNameTemp.contains("setting") || lowerCase.contains("android系统")
                            || lowerCase.contains("android 系统")) {
                        continue;
                    }

                    processInfo.setPackageName(packageNameTemp);
                    processInfo.setProcessName(androidAppProcess.name);
                    processInfo.setPid(androidAppProcess.pid);
                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                    processInfo.setAppName(applicationLabel.toString());
                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
                    processInfo.setSystemApp(false);
                    if (!result.contains(processInfo)) {
                        result.add(processInfo);
                    }
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
            String[] arr;
            int size = runningAppProcesses.size();
            if (size == 0) {
                return null;
            }
            result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                runningAppProcessInfo = runningAppProcesses.get(i);
                if (runningAppProcessInfo == null) {
                    continue;
                }
                arr = runningAppProcessInfo.pkgList;
                if (arr == null || arr.length == 0) {
                    continue;
                }

                Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});

                processInfo = new ProcessInfo();
                for (String anArr : arr) {
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                        LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (packageNames.length != 0) {
                        for (String packageName:packageNames) {
                            if (!anArr.equals(packageName)) {
//                            LogUtil.i("------");
//                            LogUtil.i("anArr -> " + anArr);
//                            LogUtil.i("runningAppProcessInfo.processName -> " + runningAppProcessInfo.processName);
//                            LogUtil.i("applicationLabel -> " + applicationLabel);

                                String lowerCase = applicationLabel.toString().toLowerCase();
                                if (anArr.contains("setting") || lowerCase.contains("android系统")
                                        || lowerCase.contains("android 系统")) {
                                    continue;
                                }

                                processInfo.setPackageName(anArr);
                                processInfo.setProcessName(runningAppProcessInfo.processName);
                                processInfo.setPid(runningAppProcessInfo.pid);
                                processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                                processInfo.setAppName(applicationLabel.toString());
                                processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
                                processInfo.setSystemApp(false);
                                if (!result.contains(processInfo)) {
                                    result.add(processInfo);
                                }
                            }
                        }

                    } else {
                        String lowerCase = applicationLabel.toString().toLowerCase();
                        if (anArr.contains("setting") || lowerCase.contains("android系统")
                                || lowerCase.contains("android 系统")) {
                            continue;
                        }

                        processInfo.setPackageName(anArr);
                        processInfo.setProcessName(runningAppProcessInfo.processName);
                        processInfo.setPid(runningAppProcessInfo.pid);
                        processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                        processInfo.setAppName(applicationLabel.toString());
                        processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
                        processInfo.setSystemApp(false);
                        if (!result.contains(processInfo)) {
                            result.add(processInfo);
                        }
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    public static boolean isRunningApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 21) {
            List<AndroidAppProcess> androidAppProcesses = ProcessManager.getRunningAppProcesses();
            if (androidAppProcesses == null) {
                return false;
            }
            AndroidAppProcess androidAppProcess;
            int size = androidAppProcesses.size();
            if (size == 0) {
                return false;
            }
            String packageNameTemp;
            for (int i = 0; i < size; i++) {
                androidAppProcess = androidAppProcesses.get(i);
                if (androidAppProcess == null) {
                    continue;
                }
                packageNameTemp = androidAppProcess.getPackageName();
                if (packageNameTemp == null) {
                    continue;
                }

                ApplicationInfo applicationInfo;
                try {
                    applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                try {
                    applicationInfo.loadLabel(packageManager);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                if (packageNameTemp.equals(packageName)) {
                    return true;
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
            String[] arr;
            int size = runningAppProcesses.size();
            if (size == 0) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                runningAppProcessInfo = runningAppProcesses.get(i);
                if (runningAppProcessInfo == null) {
                    continue;
                }
                arr = runningAppProcessInfo.pkgList;
                if (arr == null || arr.length == 0) {
                    continue;
                }

                for (String anArr : arr) {
                    ApplicationInfo applicationInfo;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
                        applicationInfo.loadLabel(packageManager);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (anArr.equals(packageName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 获取当前应用的主进程id
     *
     * @param context
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static int obtainCurrentMainProcessId(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.processName.equals(context.getPackageName())) {
                return appProcess.pid;
            }
        }
        return -1;
    }

    /**
     * 当前应用的主进程是否运行中
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess", "unused"})
    public static boolean isRunningAppForMainProcess(Context context) {
        return obtainCurrentMainProcessId(context) != -1;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static boolean isSystemApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean status = false;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            status = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                    == ApplicationInfo.FLAG_SYSTEM;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * 跳转到当前应用的设置界面
     *
     * @param context
     */
    @SuppressWarnings("JavaDoc")
    public static void goToAppSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static long getTotalMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }

    public static long getTotalCleanMemory(Context context, long available) {
        return getTotalMemory(context) - available;
    }

    public static Typeface getSourceTypeFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSansCN-Regular.otf");
    }
}
