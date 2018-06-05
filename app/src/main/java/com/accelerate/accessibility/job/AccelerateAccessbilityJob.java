package com.accelerate.accessibility.job;

import android.app.AlertDialog;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import com.accelerate.AccelerateApplication;
import com.accelerate.accessibility.AccessibilityHelper;
import com.accelerate.accessibility.domain.NodeInfo;
import com.accelerate.constant.ConstantValue;
import com.accelerate.utils.LogUtil;
import com.boost.booster.clean.R;
import com.ccmt.library.lru.LruMap;

import java.util.List;

public class AccelerateAccessbilityJob extends AbstractAccessibiltyJob {

    /**
     * 设置应用的包名
     */
    private static final String PACKAGENAME_SETTING = "com.android.settings";

    /**
     * 自动点击操作是否可以进行下去
     */
    private boolean mIsClick;

    @Override
    protected void onTypeWindowContentChanged(AccessibilityEvent event) {
//        if ("com.android.settings.applications.InstalledAppDetailsTop".equals(mClassName)
//                && "android.widget.FrameLayout".equals(mSource)) {
//            String className = event.getClassName().toString();
//            if ("android.widget.FrameLayout".equals(className)) {
//                mIsDoFilt = true;
//                return;
//            }
//        }
//        mIsDoFilt = false;
    }

    @SuppressWarnings({"SpellCheckingInspection", "Convert2streamapi"})
    @Override
    protected void onTypeWindowStateChanged(AccessibilityEvent event, String className,
                                            String source, List<String> eventTexts) {
        LogUtil.i("eventTexts -> " + eventTexts);

        if ("com.android.settings.applications.InstalledAppDetailsTop".equals(className)
                && "android.widget.FrameLayout".equals(source)) {
//            if (mClassName != null && (mClassName.contains("ManageApplicationsActivity")
//                    || mClassName.contains("SubSettings"))) {
//                return;
//            }
            if (mClassName != null && (mClassName.contains("ManageApplicationsActivity"))) {
                return;
            }

            // 已经点击了ok按钮
            if (AlertDialog.class.getName().equals(mClassName)
                    && FrameLayout.class.getName().equals(mSource)) {
                // 如果强行停止按钮点了不生效,也就是没有变灰,那么会不停重复点击,不应该不停重复点击.
                LogUtil.i("已经对当前界面对应的应用进行强行停止了");
                doStartActivity();
                return;
            }

            AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
            List<AccessibilityNodeInfo> list = AccessibilityHelper.findNodeInfosByText(rootInActiveWindow,
                    mService.getString(R.string.accelerate_force_stop));
            LogUtil.i("强行停止 -> " + list);
            if (list != null && list.size() > 0) {
                // 强行停止
                LogUtil.i("强行停止的集合 -> " + list.size());
                if (!AccessibilityHelper.performClick(list.get(0))) {
                    doStartActivity();
                } else {
                    mIsClick = true;
                }
            } else {
                // 结束运行
                list = AccessibilityHelper.findNodeInfosByText(rootInActiveWindow,
                        mService.getString(R.string.accelerate_finish_run));
                LogUtil.i("结束运行 -> " + list);
                if (list != null && list.size() > 0) {
                    LogUtil.i("结束运行的集合 -> " + list.size());
                    if (!AccessibilityHelper.performClick(list.get(0))) {
                        doStartActivity();
                    } else {
                        mIsClick = true;
                    }
                } else {
                    // 强制停止
                    list = AccessibilityHelper.findNodeInfosByText(rootInActiveWindow,
                            mService.getString(R.string.accelerate_mandatory_stop));
                    LogUtil.i("强制停止 -> " + list);
                    if (list != null && list.size() > 0) {
                        LogUtil.i("强制停止的集合 -> " + list.size());
                        if (!AccessibilityHelper.performClick(list.get(0))) {
                            doStartActivity();
                        } else {
                            mIsClick = true;
                        }
                    }
                }
            }
        } else if ("android.app.AlertDialog".equals(className)
                && "android.widget.FrameLayout".equals(source)) {
            if (!mIsClick) {
                // 用户手动点击强行停止按钮,不应该自动点击确定按钮.
                return;
            }

            // 确定
            AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
            List<AccessibilityNodeInfo> list = AccessibilityHelper.findNodeInfosByText(rootInActiveWindow,
                    mService.getString(R.string.accelerate_ensure));
            LogUtil.i("确定 -> " + list);
            if (list != null && list.size() > 0) {
                LogUtil.i("确定的集合 -> " + list.size());
                AccessibilityHelper.performClick(list.get(list.size() - 1));
            } else {
                // list.size() - 1为了适配Samsung SM-G9200手机的确定按钮,会找出两个,必须获取最后1个.
                list = AccessibilityHelper.findNodeInfosByText(rootInActiveWindow,
                        mService.getString(R.string.accelerate_mandatory_stop));
                LogUtil.i("确定 -> " + list);
                if (list != null && list.size() > 0) {
                    LogUtil.i("确定的集合 -> " + list.size());
                    AccessibilityHelper.performClick(list.get(list.size() - 1));
                }
            }
        }
    }

    private void doStartActivity() {
        LruMap lruMap = LruMap.getInstance();
        Runnable runnable = (Runnable) lruMap.get(ConstantValue.LRU_RUNNABLE);
        if (runnable != null) {
            lruMap.remove(ConstantValue.LRU_RUNNABLE, false);
            lruMap.remove(ConstantValue.LRU_IS_CLICK, false);
            lruMap.remove(ConstantValue.LRU_PACKAGE_NAME, false);
            lruMap.remove(ConstantValue.LRU_IS_DO_TASKABLE, false);
            mIsClick = false;
            runnable.run();
        } else {
            mIsClick = true;
            AccelerateApplication.getAccelerateApplication().mHandlerAccelerate.sendEmptyMessageDelayed(0, 800);
        }
    }

    @Override
    protected boolean doFilt(AccessibilityEvent event, String className,
                             String sourceClassName, List<NodeInfo> sourceChildNodeInfos,
                             List<String> eventTexts) {
        LogUtil.i("AccelerateAccessbilityJob doFilt()");
        if (!mIsClick) {
            Boolean isClick = (Boolean) LruMap.getInstance().get(ConstantValue.LRU_IS_CLICK);
            if (isClick != null) {
                mIsClick = isClick;
            } else {
                mIsClick = false;
            }
        }
        LogUtil.i("mIsClick -> " + mIsClick);
        return mIsClick || sourceChildNodeInfos != null && !sourceChildNodeInfos.equals(mSourceChildNodeInfos);
    }

    @Override
    public String getTargetPackageName() {
        return PACKAGENAME_SETTING;
    }

}