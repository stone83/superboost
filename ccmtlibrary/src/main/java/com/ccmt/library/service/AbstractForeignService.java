package com.ccmt.library.service;

import android.content.Intent;
import android.util.Log;

import com.ccmt.library.global.Global;
import com.ccmt.library.manager.INotificationManager;
import com.ccmt.library.util.CcmtUtil;

/**
 * 前台服务基类
 *
 * @author myx
 */
public abstract class AbstractForeignService extends AbstractService {

    /**
     * 当前服务将所在进程变成前台进程时发送的通知的id
     */
//	private int foreignUuid;

//	private INotificationManager notificationManager;
    @Override
    public void onCreate() {
        super.onCreate();

        INotificationManager notificationManager = obtainNotificationManager();
        int foreignUuid = notificationManager.createUuid();
        startForeground(foreignUuid, notificationManager.createNotification());

        Log.i("MyLog", "开启通知成功");

        Class<? extends AbstractForeignService> cla = getClass();

        if (!Global.allInitForeignServices.containsKey(cla)) {
            initForeign();
            Global.allInitForeignServices.put(cla, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);

        Log.i("MyLog", "停止通知成功");
    }

    private INotificationManager obtainNotificationManager() {
        return CcmtUtil.obtainNotificationManager(this);
    }

    /**
     * 有些服务可能需要做初始化操作,因为停止服务后再启动服务,初始化操作没必要再做1次,所以该方法就是用来解决这个需求.
     */
    protected abstract void initForeign();

    protected abstract void doTask(Intent intent);

}
