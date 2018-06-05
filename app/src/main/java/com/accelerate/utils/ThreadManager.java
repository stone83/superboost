package com.accelerate.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.security.InvalidParameterException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 线程管理器，其内线程与进程同生命周期
 */

public class ThreadManager {
    /**
     * UI主线程
     */
    public static final int THREAD_UI = 0;
    /**
     * IO线程，主要执行费时操作
     */
    public static final int THREAD_IO = 1;
    /**
     * 后台处理数据线程，运行不费时操作：计算/获取数据等
     */
    public static final int THREAD_WORKER = 2;

    private static final int THREAD_SIZE = 3;

    private static final Handler[] HANDLER_LIST = new Handler[THREAD_SIZE];

    private static final Scheduler[] SCHEDULER_LIST = new Scheduler[THREAD_SIZE];

    private static final String[] THREAD_NAME_LIST
            = new String[]{"thread_ui", "thread_io", "thread_worker"};
    /**
     * 线程池
     */
    private static ExecutorService sExecutorService;

    public ThreadManager() {
    }

    public static void startup() {
        HANDLER_LIST[0] = new Handler();
        sExecutorService = Executors.newCachedThreadPool();
    }

    public static void post(int index, Runnable r) {
        postDelayed(index, r, 0L);
    }

    public static void postDelayed(int index, Runnable r, long delayMillis) {
        Handler handler = getHandler(index);
        handler.postDelayed(r, delayMillis);
    }

    public static void executeAsyncTask(Runnable runnable) {
        if (runnable != null) {
            sExecutorService.execute(runnable);
        }
    }

    public static void removeCallbacks(int index, Runnable r) {
        Handler handler = getHandler(index);
        handler.removeCallbacks(r);
    }

    private static Handler getHandler(int index) {
        if (index >= 0 && index < THREAD_SIZE) {
            if (HANDLER_LIST[index] == null) {
                synchronized (HANDLER_LIST) {
                    if (HANDLER_LIST[index] == null) {
                        HandlerThread thread = new HandlerThread(THREAD_NAME_LIST[index]);
                        if (index != 0) {
                            thread.setPriority(1);
                        }
                        thread.start();
                        Handler handler = new Handler(thread.getLooper());
                        HANDLER_LIST[index] = handler;
                    }
                }
            }
            return HANDLER_LIST[index];
        } else {
            throw new InvalidParameterException();
        }
    }

    public static Scheduler getScheduler(int index) {
        if (index >= 0 && index < THREAD_SIZE) {
            if (SCHEDULER_LIST[index] == null) {
                synchronized (SCHEDULER_LIST) {
                    if (SCHEDULER_LIST[index] == null) {
                        SCHEDULER_LIST[index] =
                                AndroidSchedulers.from(getHandler(index).getLooper());
                    }
                }
            }
            return SCHEDULER_LIST[index];
        } else {
            throw new InvalidParameterException();
        }
    }

    public static boolean runningOn(int index) {
        return getHandler(index).getLooper() == Looper.myLooper();
    }

    public static void checkThread(int index) {
        if (!runningOn(index)) {
            throw new RuntimeException("线程错误！请确保操作运行在线程：" + THREAD_NAME_LIST[index]);
        }
    }
}