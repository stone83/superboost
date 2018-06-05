package com.accelerate.accessibility.notifycation;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.accelerate.accessibility.services.AbstractAccessibilityService;
import com.accelerate.accessibility.services.AccelerateAccessibilityService;
import com.accelerate.accessibility.Config;
import com.accelerate.utils.LogUtil;
import com.ccmt.library.lru.LruMap;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AccelerateNotificationService extends NotificationListenerService {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(getClass().getSimpleName() + " onCreate()");
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        onListenerConnected();
//        }
    }

    private Config getConfig() {
        return Config.getConfig(this);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        LogUtil.i(getClass().getSimpleName() + " onNotificationPosted()");
        LogUtil.i("getConfig().isEnableNotificationService() -> " + getConfig().isEnableNotificationService());
//        if (!getConfig().isAgreement()) {
//            return;
//        }
        if (!getConfig().isEnableNotificationService()) {
            LogUtil.i("do not have notification mService");
            return;
        }
        LruMap lruMap = LruMap.getInstance();
        AbstractAccessibilityService accessibilityService = (AbstractAccessibilityService) lruMap
                .get(AccelerateAccessibilityService.class.getName());
        LogUtil.i("accessibilityService -> " + accessibilityService);
        if (accessibilityService != null) {
            accessibilityService.handeNotificationPosted(new IStatusBarNotification() {
                @Override
                public String getPackageName() {
                    return sbn.getPackageName();
                }

                @Override
                public Notification getNotification() {
                    return sbn.getNotification();
                }
            });
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        LogUtil.i(getClass().getSimpleName() + " onNotificationRemoved()");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        super.onNotificationRemoved(sbn);
//        }
    }

    @Override
    public void onListenerConnected() {
        LogUtil.i(getClass().getSimpleName() + " onListenerConnected()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onListenerConnected();
        }

        //发送广播,已经连接上了.
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i(getClass().getSimpleName() + " onBind()");
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i(getClass().getSimpleName() + " onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("AccelerateNotificationService onDestroy()");

        //发送广播,已经连接上了.
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

//    /**
//     * 是否启动通知栏监听
//     */
//    public static boolean isRunning() {
//        if (Build.VERSION.SDK_INT < 18) {
//            return false;
//        }
//
//        // 部份手机没有NotificationService服务
//        return mService != null;
//    }

    public static boolean isRunning(Context context) {
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

}
