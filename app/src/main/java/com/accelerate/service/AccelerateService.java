package com.accelerate.service;

import android.content.Intent;
import android.os.Message;
import android.os.SystemClock;

import com.accelerate.AccelerateApplication;
import com.accelerate.activity.AccelerateMainActivity;
import com.accelerate.constant.ConstantValue;
import com.accelerate.utils.CommonUtil;
import com.accelerate.utils.LogUtil;
import com.ccmt.library.lru.LruMap;
import com.ccmt.library.service.AbstractService;

import java.util.concurrent.CountDownLatch;

/**
 * @author myx
 *         by 2017-06-01
 */
public class AccelerateService extends AbstractService {

    public static final Object LOCK = new Object();
    private boolean mIsExit;
    private Boolean mIsClick;

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            mIsExit = true;
        }
    }

    @Override
    protected void doTask(Intent intent) {
        // 如果是系统重启服务,可以选择继续做任务,也可以选择不做任务,后续可能用到.
//        if (!mIsUserStart) {
//            LogUtil.i("AccelerateService被系统重启");
//            return;
//        }

        if (AccelerateApplication.application.mHandlerAccelerate == null) {
            LruMap.getInstance().put(ConstantValue.LRU_IS_RESTART_ACCELERATE_SERVICE, true);
            Intent intent2 = new Intent(this, AccelerateMainActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);
            return;
        }
        LruMap lruMap = LruMap.getInstance();
        synchronized (LOCK) {
            if (mIsDoTaskable == null) {
                return;
            }
            mIsDoTaskable = null;
        }
        LogUtil.i("AccelerateService doTask()");
        mIsExit = false;
        Runnable runnable = () -> {
            CountDownLatch countDownLatch = null;
            while (true) {
                synchronized (AccelerateService.this) {
                    if (mIsExit) {
                        synchronized (LOCK) {
                            mIsDoTaskable = true;
                        }
                        return;
                    }
                }

                countDownLatch = new CountDownLatch(1);
                CountDownLatch countDownLatchTemp = countDownLatch;
                AccelerateApplication.application.mHandlerAccelerate.post(() -> {
                    Boolean doTaskable = (Boolean) lruMap.get(ConstantValue.LRU_IS_DO_TASKABLE);
                    if (doTaskable == null) {
                        lruMap.remove(ConstantValue.LRU_RUNNABLE, false);
                        lruMap.remove(ConstantValue.LRU_IS_CLICK, false);
                        lruMap.remove(ConstantValue.LRU_PACKAGE_NAME, false);
                        lruMap.remove(ConstantValue.LRU_IS_DO_TASKABLE, false);
                        synchronized (AccelerateService.this) {
                            mIsExit = true;
                        }
                        countDownLatchTemp.countDown();
                        return;
                    }
                    mIsClick = (Boolean) lruMap.get(ConstantValue.LRU_IS_CLICK);
                    if (mIsClick != null) {
                        String packageName = (String) lruMap.get(ConstantValue.LRU_PACKAGE_NAME);
                        if (packageName != null) {
                            // 获取到包名
                            LogUtil.i("获取到包名 -> " + packageName);
                            boolean runningApp = CommonUtil.isRunningApp(AccelerateService.this, packageName);
                            LogUtil.i("是否运行中 -> " + runningApp);
                            if (!runningApp) {
                                // 强行停止成功
                                LogUtil.i("强行停止成功");
                                Runnable runnable1 = (Runnable) lruMap.get(ConstantValue.LRU_RUNNABLE);
                                if (runnable1 != null) {
                                    // 最后1个应用停止结束,跳转到主界面.
                                    LogUtil.i("最后1个应用停止结束,跳转到主界面.");
                                    lruMap.remove(ConstantValue.LRU_RUNNABLE, false);
                                    lruMap.remove(ConstantValue.LRU_IS_CLICK, false);
                                    lruMap.remove(ConstantValue.LRU_PACKAGE_NAME, false);
                                    lruMap.remove(ConstantValue.LRU_IS_DO_TASKABLE, false);
                                    runnable1.run();
                                    synchronized (AccelerateService.this) {
                                        mIsExit = true;
                                    }
                                    countDownLatchTemp.countDown();
                                } else {
                                    // 还有应用没有停止
                                    LogUtil.i("还有应用没有停止");
                                    Message message = Message.obtain();
                                    message.what = 0;
                                    message.obj = countDownLatchTemp;
                                    AccelerateApplication.application.mHandlerAccelerate.sendMessage(message);
                                }
                            } else {
                                // 强行停止失败
                                LogUtil.i("强行停止失败");
                                countDownLatchTemp.countDown();
                            }
                        } else {
                            // 没有获取到包名
                            LogUtil.i("没有获取到包名");
                            countDownLatchTemp.countDown();
                        }
                    } else {
                        // 没有点击加速按钮
                        LogUtil.i("没有点击加速按钮");
                        countDownLatchTemp.countDown();
                    }
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SystemClock.sleep(1000);
            }
        };
        Message message = Message.obtain();
        message.what = 0;
        message.obj = runnable;
        AccelerateApplication.application.mHandlerAccelerate.sendMessage(message);
    }

}
