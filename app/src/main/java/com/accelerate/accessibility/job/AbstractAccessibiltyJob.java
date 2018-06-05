package com.accelerate.accessibility.job;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.accelerate.AccelerateApplication;
import com.accelerate.accessibility.Config;
import com.accelerate.accessibility.domain.NodeInfo;
import com.accelerate.accessibility.notifycation.IStatusBarNotification;
import com.accelerate.accessibility.services.AbstractAccessibilityService;
import com.accelerate.utils.LogUtil;
import com.boost.booster.clean.R;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractAccessibiltyJob implements IAccessibilityJob {

    @SuppressWarnings("unused")
    private static final String TIP = AccelerateApplication.application.getResources().getString(R.string.app_name);

    @SuppressWarnings("WeakerAccess")
    protected AbstractAccessibilityService mService;
    private PackageInfo mPackageInfo;
    @SuppressWarnings("WeakerAccess")
    protected String mClassName;
    @SuppressWarnings("WeakerAccess")
    protected String mSource;
    @SuppressWarnings("WeakerAccess")
    protected String mCurrentActivity;
    private List<NodeInfo> mRootNodeInfos;
    @SuppressWarnings("WeakerAccess")
    protected List<NodeInfo> mSourceChildNodeInfos;
    @SuppressWarnings("WeakerAccess")
    protected List<String> mEventTexts;

    /**
     * 如果为true,会执行onTypeWindowStateChanged()方法.如果为false,不会执行onTypeWindowStateChanged()方法.
     */
    @SuppressWarnings("WeakerAccess")
    protected boolean mIsDoFilt;

    /**
     * 唤醒屏幕相关
     */
    @SuppressWarnings("unused")
    private KeyguardManager mKeyguardManager;
    @SuppressWarnings({"unused", "deprecation"})
    private KeyguardManager.KeyguardLock mKeyguardLock;
    @SuppressWarnings("unused")
    private PowerManager mPowerManager;
    @SuppressWarnings("unused")
    private PowerManager.WakeLock mWakeLock;


    @Override
    public void onCreateJob(AbstractAccessibilityService service) {
        mService = service;

        // 获取电源管理器对象
//        mPowerManager = (PowerManager) mService.getSystemService(Context.POWER_SERVICE);

        // 获取键盘锁管理器对象
//        mKeyguardManager = (KeyguardManager) mService.getSystemService(Context.KEYGUARD_SERVICE);

        updatePackageInfo();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceiveJob(AbstractAccessibilityService accessibilityService, AccessibilityEvent event) {
        LogUtil.i(getClass().getSimpleName() + " onReceiveJob()");

        mService = accessibilityService;

        boolean autoState = Config.getConfig(mService).isAutoState();
        String className = event.getClassName().toString();
        CharSequence beforeText = event.getBeforeText();
        AccessibilityNodeInfo sourceAccessibilityNodeInfo = event.getSource();
        String source;
        if (sourceAccessibilityNodeInfo != null) {
            CharSequence sourceClassName = sourceAccessibilityNodeInfo.getClassName();
            if (sourceClassName == null) {
                source = null;
            } else {
                source = sourceClassName.toString();
            }
        } else {
            source = null;
        }
        LogUtil.i("autoState -> " + autoState);
        LogUtil.i("className -> " + className);
        LogUtil.i("mCurrentActivity -> " + mCurrentActivity);
        LogUtil.i("beforeText -> " + beforeText);
        LogUtil.i("source -> " + source);

//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            List<AccessibilityWindowInfo> windows = mService.getWindows();
//            AccessibilityWindowInfo accessibilityWindowInfo;
//            AccessibilityNodeInfo root;
//            String packageName;
//            for (int i = 0; i < windows.size(); i++) {
//                accessibilityWindowInfo = windows.get(i);
//                root = accessibilityWindowInfo.getRoot();
//                packageName = root.getPackageName().toString();
//                className = root.getClassName().toString();
//                LogUtil.i("------");
//                LogUtil.i("packageName -> " + packageName);
//                LogUtil.i("className -> " + className);
//            }
//        }

        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            LogUtil.i("rootInActiveWindow -> " + null);
            return;
        }
        CharSequence packageName = rootInActiveWindow.getPackageName();
        LogUtil.i("packageName -> " + packageName);

//        List<NodeInfo> rootNodeInfos = obtainRootChildNodeInfo(rootInActiveWindow);
//        showSourceChildNodeInfo(source);
//        List<String> eventTexts = obtainEventTextInfo(event);

//        if (!autoState) {
//            return;
//        }

        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            // 当窗口内容发生改变时
            LogUtil.i("TYPE_WINDOW_CONTENT_CHANGED");
            onTypeWindowContentChanged(event);
        } else if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            // 当通知栏发生改变时
            LogUtil.i("通知栏状态发生变化");

            String tip = event.getText().toString();
            LogUtil.i("tip -> " + tip);
            if (tip.contains(TIP)) {
                Parcelable parcelable = event.getParcelableData();
                if (parcelable != null && parcelable instanceof Notification) {
                    final Notification notification = (Notification) parcelable;

                    // 解锁屏
//                    wakeAndUnlock(true);

                    try {
                        notification.contentIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            LogUtil.i("TYPE_WINDOW_STATE_CHANGED");

            // 当窗口的状态发生改变时
            List<NodeInfo> rootNodeInfos = obtainRootChildNodeInfo(rootInActiveWindow);
            List<NodeInfo> sourceChildNodeInfos = obtainSourceChildNodeInfo(sourceAccessibilityNodeInfo);
            List<String> eventTexts = obtainEventTextInfo(event);

            // 先判断className,source和eventTexts,
            // 如果当前界面的className,source和eventTexts与上个界面的className,source和eventTexts只要有1个不同,
            // 则肯定是跳到了不同的界面.要知道如果黑屏后,再亮屏,
            // 这个时候className,source和eventTexts跟黑屏前的className,source和eventTexts完全相同,
            // 就不能再处理TYPE_WINDOW_STATE_CHANGED事件.
            LogUtil.i("mClassName -> " + mClassName);
            LogUtil.i("mSource -> " + mSource);

            if (rootNodeInfos != null) {
                // rootNodeInfos不为空
                if (eventTexts != null) {
                    if (source != null) {
                        if (!className.equals(mClassName) || !source.equals(mSource)
                                || !eventTexts.equals(mEventTexts) || !rootNodeInfos.equals(mRootNodeInfos)) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, source, eventTexts);
                            mClassName = className;
                            mSource = source;
                            mCurrentActivity = className;
                            mRootNodeInfos = rootNodeInfos;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = eventTexts;
                            return;
                        }
                    } else {
                        if (!className.equals(mClassName) || mSource != null
                                || !eventTexts.equals(mEventTexts) || !rootNodeInfos.equals(mRootNodeInfos)) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, null, eventTexts);
                            mClassName = className;
                            mSource = null;
                            mCurrentActivity = className;
                            mRootNodeInfos = rootNodeInfos;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = eventTexts;
                            return;
                        }
                    }
                } else {
                    if (source != null) {
                        if (!className.equals(mClassName) || !source.equals(mSource)
                                || mEventTexts != null || !rootNodeInfos.equals(mRootNodeInfos)) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, source, null);
                            mClassName = className;
                            mSource = source;
                            mCurrentActivity = className;
                            mRootNodeInfos = rootNodeInfos;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = null;
                            return;
                        }
                    } else {
                        if (!className.equals(mClassName) || mSource != null
                                || mEventTexts != null || !rootNodeInfos.equals(mRootNodeInfos)) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, null, null);
                            mClassName = className;
                            mSource = null;
                            mCurrentActivity = className;
                            mRootNodeInfos = rootNodeInfos;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = null;
                            return;
                        }
                    }
                }
            } else {
                // rootNodeInfos为空
                if (eventTexts != null) {
                    if (source != null) {
                        if (!className.equals(mClassName) || !source.equals(mSource)
                                || !eventTexts.equals(mEventTexts) || mRootNodeInfos != null) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, source, eventTexts);
                            mClassName = className;
                            mSource = source;
                            mCurrentActivity = className;
                            mRootNodeInfos = null;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = eventTexts;
                            return;
                        }
                    } else {
                        if (!className.equals(mClassName) || mSource != null
                                || !eventTexts.equals(mEventTexts) || mRootNodeInfos != null) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, null, eventTexts);
                            mClassName = className;
                            mSource = null;
                            mCurrentActivity = className;
                            mRootNodeInfos = null;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = eventTexts;
                            return;
                        }
                    }
                } else {
                    if (source != null) {
                        if (!className.equals(mClassName) || !source.equals(mSource)
                                || mEventTexts != null || mRootNodeInfos != null) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, source, null);
                            mClassName = className;
                            mSource = source;
                            mCurrentActivity = className;
                            mRootNodeInfos = null;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = null;
                            return;
                        }
                    } else {
                        if (!className.equals(mClassName) || mSource != null
                                || mEventTexts != null || mRootNodeInfos != null) {
                            mIsDoFilt = true;
                            onTypeWindowStateChanged(event, className, null, null);
                            mClassName = className;
                            mSource = null;
                            mCurrentActivity = className;
                            mRootNodeInfos = null;
                            mSourceChildNodeInfos = sourceChildNodeInfos;
                            mEventTexts = null;
                            return;
                        }
                    }
                }
            }

            // 上个界面和当前界面完全相同时,如果doFilt()方法返回true代表仍然要调用onTypeWindowStateChanged()方法.
            // 如果返回false,不调用onTypeWindowStateChanged()方法.
            LogUtil.i("上个界面和当前界面完全相同");
            if (doFilt(event, className, source, sourceChildNodeInfos, eventTexts)) {
                LogUtil.i("过滤操作执行完,调用onTypeWindowStateChanged()方法.");
                mIsDoFilt = true;
                onTypeWindowStateChanged(event, className, source, eventTexts);
                mClassName = className;
                mSource = source;
                mCurrentActivity = className;
                mRootNodeInfos = rootNodeInfos;
                mSourceChildNodeInfos = sourceChildNodeInfos;
                mEventTexts = eventTexts;
                mIsDoFilt = false;
                return;
            }
            LogUtil.i("过滤操作执行完,不调用onTypeWindowStateChanged()方法.");

            mIsDoFilt = false;
            mClassName = className;
            mSource = source;
            mCurrentActivity = className;
            mRootNodeInfos = rootNodeInfos;
            mSourceChildNodeInfos = sourceChildNodeInfos;
            mEventTexts = eventTexts;
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            LogUtil.i("TYPE_VIEW_CLICKED");
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            LogUtil.i("TYPE_VIEW_LONG_CLICKED");
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            LogUtil.i("TYPE_VIEW_FOCUSED");
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            LogUtil.i("TYPE_VIEW_TEXT_CHANGED");
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            LogUtil.i("TYPE_VIEW_TEXT_SELECTION_CHANGED");
        } else if (eventType == AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START) {
            LogUtil.i("TYPE_TOUCH_EXPLORATION_GESTURE_START");
        } else if (eventType == AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END) {
            LogUtil.i("TYPE_TOUCH_EXPLORATION_GESTURE_END");
        } else if (eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START) {
            LogUtil.i("TYPE_TOUCH_INTERACTION_START");
        } else if (eventType == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END) {
            LogUtil.i("TYPE_TOUCH_INTERACTION_END");
        } else if (eventType == AccessibilityEvent.TYPE_GESTURE_DETECTION_START) {
            LogUtil.i("TYPE_GESTURE_DETECTION_START");
        } else if (eventType == AccessibilityEvent.TYPE_GESTURE_DETECTION_END) {
            LogUtil.i("TYPE_GESTURE_DETECTION_END");
        } else if (eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            LogUtil.i("TYPE_WINDOWS_CHANGED");
        }
    }

    protected abstract boolean doFilt(AccessibilityEvent event, String className,
                                      String source, List<NodeInfo> sourceChildNodeInfos, List<String> eventTexts);

    private List<NodeInfo> obtainRootChildNodeInfo(AccessibilityNodeInfo rootInActiveWindow) {
        List<NodeInfo> result = null;
        AccessibilityNodeInfo accessibilityNodeInfo;
        CharSequence temp;
        String text;
        int childCount = rootInActiveWindow.getChildCount();
        CharSequence rootInActiveWindowClassName = rootInActiveWindow.getClassName();
        LogUtil.i("rootInActiveWindowClassName.toString() -> " + rootInActiveWindowClassName.toString());
        LogUtil.i("rootInActiveWindow.getText() -> " + rootInActiveWindow.getText());
        LogUtil.i("rootInActiveWindow.getContentDescription() -> " + rootInActiveWindow.getContentDescription());
        LogUtil.i("root子节点的数量 -> " + childCount);
        if (childCount > 0) {
            result = new ArrayList<>();
            NodeInfo nodeInfo;
            for (int i = 0; i < childCount; i++) {
                nodeInfo = new NodeInfo();
                accessibilityNodeInfo = rootInActiveWindow.getChild(i);
                if (accessibilityNodeInfo == null) {
                    continue;
                }
                String className = accessibilityNodeInfo.getClassName().toString();
                LogUtil.i("------");
                LogUtil.i("root子节点的className -> " + className);
                nodeInfo.className = className;
                temp = accessibilityNodeInfo.getText();
                if (temp != null) {
                    text = temp.toString();
                    LogUtil.i("root子节点的text -> " + text);
                    nodeInfo.text = text;
                } else {
                    nodeInfo.text = "";
                }
                temp = accessibilityNodeInfo.getContentDescription();
                if (temp != null) {
                    text = temp.toString();
                    LogUtil.i("root子节点的contentDescription -> " + text);
                    nodeInfo.contentDescription = text;
                } else {
                    nodeInfo.contentDescription = "";
                }
            }
        }
        return result;
    }

    private List<String> obtainEventTextInfo(AccessibilityEvent event) {
        List<String> result;
        String text;
        List<CharSequence> texts = event.getText();
        LogUtil.i("texts -> " + texts);
        if (texts == null || texts.size() == 0) {
            return null;
        }
        result = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            text = texts.get(i).toString();
            LogUtil.i("------");
            LogUtil.i("事件的text -> " + text);
            result.add(text);
        }
        return result;
    }

    private List<NodeInfo> obtainSourceChildNodeInfo(AccessibilityNodeInfo source) {
        List<NodeInfo> result = null;
        String text;
        AccessibilityNodeInfo accessibilityNodeInfo;
        CharSequence temp;
        if (source != null) {
//            LogUtil.i("source.getClassName().toString() -> " + source.getClassName().toString());
            LogUtil.i("source.getText() -> " + source.getText());
            LogUtil.i("source.getContentDescription() -> " + source.getContentDescription());
            int childCount = source.getChildCount();
            LogUtil.i("source子节点的数量 -> " + childCount);
            if (childCount > 0) {
                result = new ArrayList<>();
                NodeInfo nodeInfo;
                String className;
                for (int i = 0; i < childCount; i++) {
                    nodeInfo = new NodeInfo();
                    accessibilityNodeInfo = source.getChild(i);
                    if (accessibilityNodeInfo == null) {
                        continue;
                    }
                    LogUtil.i("------");
                    className = accessibilityNodeInfo.getClassName().toString();
                    LogUtil.i("source子节点的className -> " + className);
                    nodeInfo.className = className;
                    temp = accessibilityNodeInfo.getText();
                    if (temp != null) {
                        text = temp.toString();
                        LogUtil.i("source子节点的text -> " + text);
                        nodeInfo.text = text;
                    } else {
                        nodeInfo.text = "";
                    }
                    temp = accessibilityNodeInfo.getContentDescription();
                    if (temp != null) {
                        text = temp.toString();
                        LogUtil.i("source子节点的contentDescription -> " + text);
                        nodeInfo.contentDescription = text;
                    } else {
                        nodeInfo.contentDescription = "";
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void onStopJob() {
        LogUtil.i(getClass().getSimpleName() + " onStopJob()");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(IStatusBarNotification sbn) {
//        Notification nf = sbn.getNotification();
//        String text = String.valueOf(sbn.getNotification().tickerText);
//        notificationEvent(text, nf);

        LogUtil.i(getClass().getSimpleName() + " onNotificationPosted()");

        // 通过以下方式可以获取Notification的详细信息
        LogUtil.showStatusBarNotificationInfo(sbn);
    }

    @Override
    public boolean isEnable() {
        return true;
    }

//    @SuppressWarnings({"deprecation", "unused"})
//    private void wakeAndUnlock(boolean wake) {
//        // 是否需要解锁密码
//        boolean isNeedPasswd = mKeyguardManager.isKeyguardSecure();
//
//        // 是否已经锁屏
//        boolean isLocked = mKeyguardManager.isKeyguardLocked();
//
//        if (isNeedPasswd || !isLocked) {
//            return;
//        }
//        if (wake) {
//            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是调试用的Tag.
//            mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
//                    | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
//
//            // 点亮屏幕
//            mWakeLock.acquire();
//            mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock");
//
//            // 解除锁屏
//            mKeyguardLock.disableKeyguard();
//        } else {
//            // 反解除锁屏
//            mKeyguardLock.reenableKeyguard();
//
//            // 释放wakeLock,关灯.
//            mWakeLock.release();
//        }
//    }

    /**
     * 获取要操作的app的版本
     */
    @SuppressWarnings("unused")
    private int getPackageVersion() {
        if (mPackageInfo == null) {
            return 0;
        }
        return mPackageInfo.versionCode;
    }

    /**
     * 更新要操作的app的包信息
     */
    private void updatePackageInfo() {
        try {
            mPackageInfo = mService.getPackageManager().getPackageInfo(getTargetPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    protected abstract void onTypeWindowContentChanged(AccessibilityEvent event);

    protected abstract void onTypeWindowStateChanged(AccessibilityEvent event, String className,
                                                     String source, List<String> eventTexts);

}
