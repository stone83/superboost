package com.accelerate.accessibility.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.accelerate.accessibility.AccessibilityHelper;
import com.accelerate.accessibility.job.AccelerateAccessbilityJob;
import com.accelerate.accessibility.job.IAccessibilityJob;
import com.accelerate.accessibility.notifycation.AccelerateNotificationService;
import com.accelerate.accessibility.notifycation.IStatusBarNotification;
import com.accelerate.utils.LogUtil;
import com.ccmt.library.lru.LruMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AbstractAccessibilityService extends AccessibilityService {

    @SuppressWarnings("SpellCheckingInspection")
    private static final Class[] ACCESSBILITY_JOBS = {
            AccelerateAccessbilityJob.class
    };

    //    public static AbstractAccessibilityService sService;
    protected boolean mIsRunning;
    protected List<IAccessibilityJob> mAccessbilityJobs;
    protected HashMap<String, IAccessibilityJob> mPkgAccessbilityJobMap;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(getClass().getSimpleName() + " onCreate()");
        mAccessbilityJobs = new ArrayList<>();
        mPkgAccessbilityJobMap = new HashMap<>();

        // 初始化辅助插件工作
        IAccessibilityJob job;
        for (Class cla : ACCESSBILITY_JOBS) {
            try {
                Object object = cla.newInstance();
                if (object instanceof IAccessibilityJob) {
                    job = (IAccessibilityJob) object;
                    job.onCreateJob(this);
                    mAccessbilityJobs.add(job);
                    mPkgAccessbilityJobMap.put(job.getTargetPackageName(), job);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i("e -> " + e);
            }
        }
    }

    @SuppressWarnings({"SpellCheckingInspection", "Convert2streamapi"})
    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(getClass().getSimpleName() + " onDestroy()");
        if (mPkgAccessbilityJobMap != null) {
            mPkgAccessbilityJobMap.clear();
        }
        if (mAccessbilityJobs != null && !mAccessbilityJobs.isEmpty()) {
            for (IAccessibilityJob job : mAccessbilityJobs) {
                job.onStopJob();
            }
            mAccessbilityJobs.clear();
        }

        mAccessbilityJobs = null;
        mPkgAccessbilityJobMap = null;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        LogUtil.i(getClass().getSimpleName() + " onServiceConnected()");
        LruMap.getInstance().put(getClass().getName(), this);
        mIsRunning = true;

        boolean running = isRunning();
        LogUtil.i("running -> " + running);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i(getClass().getSimpleName() + " onUnbind()");

        mIsRunning = false;
        LruMap.getInstance().remove(getClass().getName(), false);

        boolean running = isRunning();
        LogUtil.i("running -> " + running);

        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {
        LogUtil.i(getClass().getSimpleName() + " onInterrupt()");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);

        LogUtil.i(getClass().getSimpleName() + " onRebind()");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pkn = String.valueOf(event.getPackageName());
        Log.i("MyLog", "pkn -> " + pkn);
        if (mAccessbilityJobs != null && !mAccessbilityJobs.isEmpty()) {
            for (IAccessibilityJob job : mAccessbilityJobs) {
                String targetPackageName = job.getTargetPackageName();
                boolean enable = job.isEnable();
                Log.i("MyLog", "targetPackageName -> " + targetPackageName);
                Log.i("MyLog", "enable -> " + enable);
                if (pkn.equals(targetPackageName) && enable) {
                    job.onReceiveJob(this, event);
                }
            }
        }
    }

    /**
     * 接收通知栏事件
     */
    @SuppressWarnings("unused")
    public void handeNotificationPosted(IStatusBarNotification notificationService) {
        LogUtil.i("AccelerateAccessibilityService handeNotificationPosted()");
        if (notificationService == null) {
            return;
        }
        if (!mIsRunning || mPkgAccessbilityJobMap == null) {
            return;
        }
        String pack = notificationService.getPackageName();
        IAccessibilityJob job = mPkgAccessbilityJobMap.get(pack);
        if (job == null) {
            return;
        }
        job.onNotificationPosted(notificationService);
    }

    /**
     * 判断当前服务是否正在运行
     */
    @SuppressWarnings("unused")
    public boolean isRunning() {
        if (mIsRunning) {
            return true;
        }
        boolean serviceEnable = AccessibilityHelper.isServiceEnable(this, this);
        if (serviceEnable) {
            mIsRunning = true;
        }
        return serviceEnable;
    }

    /**
     * 快速读取通知栏服务是否启动
     */
    @SuppressWarnings("unused")
    public boolean isNotificationServiceRunning() {
        return AccelerateNotificationService.isRunning(this);
    }

}
