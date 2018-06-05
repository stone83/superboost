package com.accelerate.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.accelerate.accessibility.services.AbstractAccessibilityService;
import com.accelerate.utils.LogUtil;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public final class AccessibilityHelper {

    private static final Field sSourceNodeField;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = //
            "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    static {
        Field field = null;
        try {
            field = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sSourceNodeField = field;
    }

    private AccessibilityHelper() {

    }

//    public static void setAccessibilitySettingsOff(Context mContext) {
//        Settings.Secure.putInt(mContext.getApplicationContext().getContentResolver(),
//                Settings.Secure.ACCESSIBILITY_ENABLED, 0);
//    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static boolean isAccessibilitySettingsOn(Context context, Class<? extends Service> cla) {
        int accessibilityEnabled = 0;
//        final String service = "com.stone.redenvlopes/com.stone.redenvlopes.service.GrabRedEnvelopeService";
        final String service = context.getPackageName() + "/" + cla.getName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            LogUtil.i("accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            LogUtil.i("Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            LogUtil.i("***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    LogUtil.i("-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        LogUtil.i("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            LogUtil.i("***ACCESSIBILIY IS DISABLED***");
//            return GrabRedEnvelopeService.isRunning();
        }

        return false;
    }

    @SuppressWarnings("unused")
    public static boolean isServiceEnable(Context context, Class<? extends Service> cla) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        String id;
        for (AccessibilityServiceInfo info : accessibilityServices) {
            id = info.getId();
            if (id.equals(context.getPackageName() + "/.accessibility.services." + cla.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isServiceEnable(Context context, AbstractAccessibilityService accessibilityService) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = accessibilityService.getServiceInfo();
        if (info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if (i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        return isConnect;
    }

    @SuppressWarnings("unused")
    public static boolean isNotificationServiceRunning(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    public static ArrayList<String> getProcessCache(Context context) {
//        ActivityManager am = (ActivityManager) context
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> sInfos = am.getRunningServices(Integer.MAX_VALUE);
//        ArrayList<String> processList = new ArrayList<String>();
//        for (ActivityManager.RunningServiceInfo sInfo : sInfos) {
//            int pid = sInfo.pid;
//            String packageName = getPackageNameName(sInfo.process);
//            if (pid > 10000 && pid != android.os.Process.myPid() && !packageName.equals("com.android.settings")
//                    && !processList.contains(packageName)) {
//                processList.add(packageName);
//            }
//        }
//        return processList;
//    }

//    public static String getPackageNameName(String process) {
//        if (process.contains(":")) {
//            process = process.split(":")[0];
//        }
//        return process;
//    }

    /**
     * 打开辅助服务的设置
     *
     * @param context
     */
    @SuppressWarnings("JavaDoc")
    public static void openAccessibilityServiceSettings(Context context) {
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    /**
     * 打开通知设置
     *
     * @param context
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static void openNotificationAccess(Context context) {
        context.startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    /**
     * 通过id查找
     */
    @SuppressWarnings("unused")
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    /**
     * 通过文本查找
     */
    public static List<AccessibilityNodeInfo> findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }

    /**
     * 通过文本查找
     */
    @SuppressWarnings("WeakerAccess")
    public static AccessibilityNodeInfo findNodeInfoByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 通过关键字查找
     */
    @SuppressWarnings("unused")
    public static AccessibilityNodeInfo findNodeInfoByTexts(AccessibilityNodeInfo nodeInfo, String... texts) {
        for (String key : texts) {
            AccessibilityNodeInfo info = findNodeInfoByText(nodeInfo, key);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    /**
     * 通过组件名字查找
     */
    @SuppressWarnings("unused")
    public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if (className.equals(node.getClassName())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 找父组件
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static AccessibilityNodeInfo findParentNodeInfosByClassName(AccessibilityNodeInfo nodeInfo,
                                                                       String className) {
        if (nodeInfo == null) {
            return null;
        }
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        if (className.equals(nodeInfo.getClassName())) {
            return nodeInfo;
        }
        return findParentNodeInfosByClassName(nodeInfo.getParent(), className);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static AccessibilityNodeInfo findView(String msg, AccessibilityNodeInfo node,
                                                 Class<? extends View> cla) {
        if (node == null) {
            return null;
        }
        // 非layout元素
        if (node.getChildCount() == 0) {
            if (cla != null) {
                if (cla.getName().equals(node.getClassName())) {
                    return obtainAccessibilityNodeInfo(msg, node);
                } else {
                    return null;
                }
            } else {
                return obtainAccessibilityNodeInfo(msg, node);
            }
        }
        // layout元素
        AccessibilityNodeInfo textView;
        for (int i = 0; i < node.getChildCount(); i++) {
            textView = findView(msg, node.getChild(i), cla);
            if (textView != null) {
                return textView;
            }
        }
        return null;
    }

    private static AccessibilityNodeInfo obtainAccessibilityNodeInfo(String msg, AccessibilityNodeInfo node) {
        CharSequence temp = node.getText();
        if (temp == null) {
            return null;
        }
        String text = temp.toString().trim();
        if (text.equals(msg)) {
            return node;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static long getSourceNodeId(AccessibilityNodeInfo nodeInfo) {
        if (sSourceNodeField == null) {
            return -1;
        }
        try {
            return sSourceNodeField.getLong(nodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public static String getViewIdResourceName(AccessibilityNodeInfo nodeInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return nodeInfo.getViewIdResourceName();
        }
        return null;
    }

    /**
     * 返回主界面事件
     */
    @SuppressWarnings("unused")
    public static void performHome(AccessibilityService service) {
        if (service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    /**
     * 返回事件
     */
    @SuppressWarnings("unused")
    public static void performBack(AccessibilityService service) {
        if (service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 点击事件
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean performClick(AccessibilityNodeInfo nodeInfo, boolean clickParent) {
        if (nodeInfo == null) {
            return false;
        }

        if (nodeInfo.isEnabled() && nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        } else {
            return clickParent && performClick(nodeInfo.getParent(), true);
        }
    }

    public static boolean performClick(AccessibilityNodeInfo nodeInfo) {
        return performClick(nodeInfo, false);
    }

}
