package com.accelerate.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.UserManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemUtil {

    static String getDeviceModel() {
        String deviceModel = null;
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            deviceModel = (String) getMethod.invoke(classType, "ro.product.model");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceModel;
    }

    /**
     * 解决InputMethodManager类的内存泄露问题
     *
     * @param application
     */
    @SuppressWarnings({"TryWithIdenticalCatches", "JavaDoc"})
    public static void fixMemoryLeak(Application application) {
        // Don't know about other versions yet.
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 || Build.VERSION.SDK_INT > 23) {
//            return;
//        }

        final InputMethodManager inputMethodManager =
                (InputMethodManager) application.getSystemService(Context.INPUT_METHOD_SERVICE);

        final Field servedViewField;
//        final Field mHField;
        final Method finishInputLockedMethod;
        final Method focusInMethod;
        try {
            servedViewField = InputMethodManager.class.getDeclaredField("mServedView");
            servedViewField.setAccessible(true);
//            mHField = InputMethodManager.class.getDeclaredField("mServedView");
//            mHField.setAccessible(true);
            finishInputLockedMethod = InputMethodManager.class.getDeclaredMethod("finishInputLocked");
            finishInputLockedMethod.setAccessible(true);
            focusInMethod = InputMethodManager.class.getDeclaredMethod("focusIn", View.class);
            focusInMethod.setAccessible(true);
        } catch (NoSuchMethodException unexpected) {
            return;
        } catch (NoSuchFieldException unexpected) {
            return;
        }
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                fixInputMethodManagerLeak(activity);
                fixUserManagerLeak(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ReferenceCleaner cleaner = new ReferenceCleaner(inputMethodManager, servedViewField,
                        finishInputLockedMethod);
                View rootView = activity.getWindow().getDecorView().getRootView();
                ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalFocusChangeListener(cleaner);
            }
        });
    }

    private static class ReferenceCleaner
            implements MessageQueue.IdleHandler, View.OnAttachStateChangeListener,
            ViewTreeObserver.OnGlobalFocusChangeListener {
        private final InputMethodManager mInputMethodManager;
        //        private final Field mHField;
        private final Field mServedViewField;
        private final Method mFinishInputLockedMethod;

        ReferenceCleaner(InputMethodManager inputMethodManager, Field servedViewField,
                         Method finishInputLockedMethod) {
            this.mInputMethodManager = inputMethodManager;
//            this.mHField = mHField;
            this.mServedViewField = servedViewField;
            this.mFinishInputLockedMethod = finishInputLockedMethod;
        }

        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            if (newFocus == null) {
                return;
            }
            if (oldFocus != null) {
                oldFocus.removeOnAttachStateChangeListener(this);
            }
            Looper.myQueue().removeIdleHandler(this);
            newFocus.addOnAttachStateChangeListener(this);
        }

        @Override
        public void onViewAttachedToWindow(View v) {

        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            v.removeOnAttachStateChangeListener(this);
            Looper.myQueue().removeIdleHandler(this);
            Looper.myQueue().addIdleHandler(this);
        }

        @Override
        public boolean queueIdle() {
            clearInputMethodManagerLeak();
            return false;
        }

        @SuppressWarnings({"TryWithIdenticalCatches", "SynchronizationOnLocalVariableOrMethodParameter"})
        private void clearInputMethodManagerLeak() {
            try {
                View servedView = (View) mServedViewField.get(mInputMethodManager);
                // This is highly dependent on the InputMethodManager implementation.
                if (servedView != null) {
                    synchronized (servedView) {
                        boolean servedViewAttached = servedView.getWindowVisibility() != View.GONE;

                        if (servedViewAttached) {
                            // The view held by the IMM was replaced without a global focus change. Let's make
                            // sure we get notified when that view detaches.

                            // Avoid double registration.
                            servedView.removeOnAttachStateChangeListener(this);
                            servedView.addOnAttachStateChangeListener(this);
                        } else {
                            // servedView is not attached. InputMethodManager is being stupid!
                            Activity activity = extractActivity(servedView.getContext());
                            if (activity == null || activity.getWindow() == null) {
                                // Unlikely case. Let's finish the input anyways.
                                try {
                                    mFinishInputLockedMethod.invoke(mInputMethodManager);
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                View decorView = activity.getWindow().peekDecorView();
                                boolean windowAttached = decorView.getWindowVisibility() != View.GONE;
                                if (!windowAttached) {
                                    mFinishInputLockedMethod.invoke(mInputMethodManager);
                                } else {
                                    decorView.requestFocusFromTouch();
                                }
                            }
                        }
                    }
                }
            } catch (IllegalAccessException unexpected) {
                unexpected.printStackTrace();
            } catch (InvocationTargetException unexpected) {
                unexpected.printStackTrace();
            }
        }

        private Activity extractActivity(Context context) {
            while (true) {
                if (context instanceof Application) {
                    return null;
                } else if (context instanceof Activity) {
                    return (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    Context baseContext = ((ContextWrapper) context).getBaseContext();
                    // Prevent Stack Overflow.
                    if (baseContext == context) {
                        return null;
                    }
                    context = baseContext;
                } else {
                    return null;
                }
            }
        }
    }

    @SuppressWarnings({"TryWithIdenticalCatches"})
    private static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
//        String[] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView", "mLastSrvView"};
//        String[] arr = new String[]{"mServedView", "mNextServedView", "mLastSrvView"};
        String[] arr = new String[]{"mLastSrvView"};
        Field f;
        Object objGet;
//        View viewGet;
//        Field contextField;
//        Object value;
        for (String param : arr) {
            try {
                f = imm.getClass().getDeclaredField(param);
                f.setAccessible(true);
                objGet = f.get(imm);
                if (objGet != null) {
//                    if (objGet instanceof View) {
//                        viewGet = (View) objGet;

//                        LogUtil.i("destContext -> " + destContext);
//                        LogUtil.i(f + " -> " + viewGet.getContext());

//                        if (v_get.getContext() == destContext) {
//                            // 被InputMethodManager持有引用的context是想要目标销毁的,置空,破坏掉path to gc节点.
//                            f.set(imm, null);
//                        } else {
//                            // 不是想要目标销毁的,即为又进了另一层界面了,不要处理,避免影响原逻辑,也就不用继续for循环了.
//                            break;
//                        }

//                        contextField = viewGet.getClass().getDeclaredField("mContext");
//                        contextField.setAccessible(true);
//                        value = contextField.get(viewGet);
//                        if (value != null) {
//                            contextField.set(viewGet, null);
//                        }
//                    }
                    f.set(imm, null);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static void fixUserManagerLeak(Context destContext) {
        if (Build.VERSION.SDK_INT < 17) {
            return;
        }

        if (destContext == null) {
            return;
        }

        UserManager userManager = (UserManager) destContext.getSystemService(Context.USER_SERVICE);
        if (userManager == null) {
            return;
        }
        String[] arr = new String[]{"mContext"};
        Field f;
        Object objGet;
//        View viewGet;
//        Field contextField;
//        Object value;
        for (String param : arr) {
            try {
                f = userManager.getClass().getDeclaredField(param);
                f.setAccessible(true);
                objGet = f.get(userManager);
                if (objGet != null) {
                    f.set(userManager, null);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
