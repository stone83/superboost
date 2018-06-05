
package com.accelerate;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Process;

import com.accelerate.activity.AccelerateMainActivity;
import com.accelerate.utils.CommonUtil;
import com.accelerate.utils.LogUtil;
import com.accelerate.utils.SystemUtil;
import com.boost.booster.clean.BuildConfig;
import com.ccmt.library.global.Global;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccelerateApplication extends Application {

    public static AccelerateApplication application;
    private ExecutorService executorService;
    private List<Activity> allActivities;
    private RefWatcher mRefWatcher;
    private Handler handler = new Handler();
    public AccelerateMainActivity.AccelerateHandler mHandlerAccelerate;

    /**
     * 每个Activity和Fragment的onDestroy()方法被调用时调用该方法
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static RefWatcher getRefWatcher() {
        return ((AccelerateApplication) application.getApplicationContext()).mRefWatcher;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        if (LeakCanary.isInAnalyzerProcess(application)) {
            return;
        }
        mRefWatcher = LeakCanary.install(application);

        // 解决InputMethodManager类的内存泄露问题
        SystemUtil.fixMemoryLeak(this);

        //Tencent Bugly 初始化 第三个参数，测试阶段建议设置成true，发布时设置为false
        CrashReport.initCrashReport(getApplicationContext(), "a35ee3e22", false);

        // 由于有运行在其他进程的组件,所以如果当前运行的进程如果不是主进程,就不用再做初始化操作.
        int pid = CommonUtil.obtainCurrentMainProcessId(this);
        if (Process.myPid() != pid) {
            return;
        }

        //EventBus索引生成，只在主进程初始化一次
//        if (TextUtils.equals(CommonUtil.getCurrentProcessName(application), getPackageName())) {
//            EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
//        }

        LogUtil.i("AccelerateApplication onCreate()");

        Global.serializableFileDir = getFileStreamPath("Ser").getAbsolutePath();
        Global.serializableFileDirNotDelete = getFileStreamPath("SerNotDelete")
                .getAbsolutePath();
        // Global.serializableFileDir = getFilesDir().getAbsolutePath()
        // + File.separator + "Ser";
        // Global.serializableFileDirNotDelete = getFilesDir().getAbsolutePath()
        // + File.separator + "SerNotDelete";

        executorService = Executors.newCachedThreadPool();

        if (!BuildConfig.IS_DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                // LogUtil.i("ex.getMessage() -> " + ex.getMessage());
                // LogUtil.i("ex.getLocalizedMessage() -> "
                // + ex.getLocalizedMessage());
                // LogUtil.i("ex.getStackTrace() -> "
                // + Arrays.toString(ex.getStackTrace()));

                if (executorService != null
                        && !executorService.isShutdown()) {
                    executorService.shutdown();
                    executorService = null;
                }

                if (allActivities != null) {
                    Iterator<Activity> ite = allActivities.iterator();
                    while (ite.hasNext()) {
                        ite.next().onBackPressed();
                        ite.remove();
                    }
                    allActivities = null;
                }

                if (Global.allRunningServices != null) {
                    Iterator<Class<? extends Service>> ite = Global.allRunningServices
                            .iterator();
                    while (ite.hasNext()) {
                        stopService(new Intent(application, ite.next()));
                        ite.remove();
                    }
                    Global.allRunningServices = null;
                }

                Process.killProcess(Process.myPid());
            });
        }

//        FileUtil.deleteDir(StorageUtil.getIncrementalUpdatingDir(application));

        allActivities = new ArrayList<>();
    }

//    /**
//     * 每次进程启动时,都会调用该方法,而且在onCreate()方法之前被调用,主要用来dex突破65535的限制.
//     *
//     * @param base
//     */
//    @SuppressWarnings("JavaDoc")
//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//
//        if (getPackageName().equals(CommonUtil.getProcessName(this))) {
////            MultiDex.install(this);
//        }
//    }

    public static AccelerateApplication getAccelerateApplication() {
        return application;
    }

    @SuppressWarnings("unused")
    public void executeAsyncTask(Runnable backgroundRunnable) {
        if (backgroundRunnable != null) {
            executorService.execute(backgroundRunnable);
        }
    }

    @SuppressWarnings("unused")
    public void post(Runnable runnable) {
        if (runnable != null) {
            handler.post(runnable);
        }
    }

//    public boolean addActivity(Activity activity) {
//        if (allActivities == null) {
//            allActivities = new ArrayList<>();
//        }
//        return allActivities.add(activity);
//    }

//    public boolean removeActivity(Activity activity) {
//        return allActivities.remove(activity);
//    }

}
