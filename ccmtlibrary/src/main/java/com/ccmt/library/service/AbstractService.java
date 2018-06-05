package com.ccmt.library.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ccmt.library.global.Global;

public abstract class AbstractService extends Service {

    public static final String KEY_IS_USER_START = "key_is_user_start";
    public static final int VALUE_IS_USER_START_USER = 1;
    @SuppressWarnings("unused")
    public static final int VALUE_IS_USER_START_SYSTEM = 2;
    protected Boolean mIsDoTaskable = true;

    /**
     * 当前服务是否被调用者开启,因为当进程被杀后,服务默认会被系统重新开启,
     * 该属性用来区分服务是被系统开启还是被调用者开启.
     */
    protected boolean mIsUserStart;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyLog", getClass().getName() + " onCreate()");
        Class<? extends AbstractService> cla = getClass();
        if (!Global.allRunningServices.contains(cla)) {
            Global.allRunningServices.add(cla);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("MyLog", getClass().getName() + " onDestroy()");
        Class<? extends AbstractService> cla = getClass();
        if (Global.allRunningServices.contains(cla)) {
            Global.allRunningServices.remove(cla);
        }
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int onStartCommandReturnValue = obtainOnStartCommandReturnValue(intent, flags, startId);
        if (onStartCommandReturnValue == Service.START_STICKY
                || onStartCommandReturnValue == Service.START_REDELIVER_INTENT) {
            mIsUserStart = !(intent == null || intent.getIntExtra(KEY_IS_USER_START,
                    VALUE_IS_USER_START_USER) != VALUE_IS_USER_START_USER);
        }
        Object obj = this;
        if (!(obj instanceof AbstractForeignService)) {
            doTask(intent);
        } else {
            doForeignTask(intent);
        }
        return onStartCommandReturnValue;
    }

    protected abstract void doTask(Intent intent);

    @SuppressWarnings("UnusedParameters")
    protected void doForeignTask(Intent intent) {

    }

    protected int obtainOnStartCommandReturnValue(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
