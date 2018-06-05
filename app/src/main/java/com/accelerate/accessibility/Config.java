package com.accelerate.accessibility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class Config {

    public static final String ACTION_QIANGHONGBAO_SERVICE_DISCONNECT = "com.stone.redenvlopes.ACCESSBILITY_DISCONNECT";
    public static final String ACTION_QIANGHONGBAO_SERVICE_CONNECT = "com.stone.redenvlopes.ACCESSBILITY_CONNECT";

    public static final String ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT = "com.stone.redenvlopes.NOTIFY_LISTENER_DISCONNECT";
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_CONNECT = "com.stone.redenvlopes.NOTIFY_LISTENER_CONNECT";

    private static final String PREFERENCE_NAME = "config";

    private static final String KEY_NOTIFICATION_SERVICE_ENABLE = "KEY_NOTIFICATION_SERVICE_ENABLE";

    private static final String KEY_NOTIFY_SOUND = "KEY_NOTIFY_SOUND";
    private static final String KEY_NOTIFY_VIBRATE = "KEY_NOTIFY_VIBRATE";
    private static final String KEY_NOTIFY_NIGHT_ENABLE = "KEY_NOTIFY_NIGHT_ENABLE";

    private static final String KEY_AGREEMENT = "KEY_AGREEMENT";

    private static final String IS_AUTO_OPEN = "IS_AUTO_OPEN";

    private static Config current;

    public static Config getConfig(Context context) {
        if (current == null) {
            synchronized (Config.class) {
                if (current == null) {
                    current = new Config(context.getApplicationContext());
                }
            }
        }
        return current;
    }

    private SharedPreferences preferences;

    private Config(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 设置自动模式
     */
    @SuppressLint("ApplySharedPref")
    @SuppressWarnings("unused")
    public void setAutoState(boolean isAutoStart) {
        preferences.edit().putBoolean(IS_AUTO_OPEN, isAutoStart).commit();
    }

    public boolean isAutoState() {
        return preferences.getBoolean(IS_AUTO_OPEN, true);
    }

    /**
     * 是否启动通知栏模式
     */
    public boolean isEnableNotificationService() {
        return preferences.getBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, false);
    }

    @SuppressLint("ApplySharedPref")
    @SuppressWarnings("unused")
    public void setNotificationServiceEnable(boolean enable) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, enable).commit();
    }

    /**
     * 是否开启声音
     */
    @SuppressWarnings("unused")
    public boolean isNotifySound() {
        return preferences.getBoolean(KEY_NOTIFY_SOUND, true);
    }

    /**
     * 是否开启震动
     */
    @SuppressWarnings("unused")
    public boolean isNotifyVibrate() {
        return preferences.getBoolean(KEY_NOTIFY_VIBRATE, true);
    }

    /**
     * 是否开启夜间免打扰模式
     */
    @SuppressWarnings("unused")
    public boolean isNotifyNight() {
        return preferences.getBoolean(KEY_NOTIFY_NIGHT_ENABLE, false);
    }

    /**
     * 免费声明
     */
    @SuppressWarnings("unused")
    public boolean isAgreement() {
        return preferences.getBoolean(KEY_AGREEMENT, false);
    }

    /**
     * 设置是否同意
     */
    @SuppressLint("ApplySharedPref")
    @SuppressWarnings("unused")
    public void setAgreement(boolean agreement) {
        preferences.edit().putBoolean(KEY_AGREEMENT, agreement).commit();
    }

}