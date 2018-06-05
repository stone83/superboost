
package com.accelerate.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.accelerate.AccelerateApplication;
import com.accelerate.utils.DimenUtils;
import com.accelerate.utils.LogUtil;
import com.accelerate.view.TitleView;
import com.boost.booster.clean.R;
import com.umeng.analytics.MobclickAgent;

public abstract class AbstractActivity extends FragmentActivity implements TitleView.OnTitleClickListener {

    protected FragmentManager mFragmentManager;
    protected Resources mResources;
    public TitleView mTitleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.i(getClass().getName() + " onCreate()");

        MobclickAgent.setDebugMode(true);
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.setCatchUncaughtExceptions(true);

        mResources = getResources();

        int customStyleResourceId = getCustomStyleResourceId();
        if (customStyleResourceId > 0) {
            setTheme(customStyleResourceId);
        }

        int layoutResID = obtainLayoutResID();
        if (layoutResID > 0) {
            setContentView(layoutResID);
        }

        setFillWindow(this);
        setForNavigationBar();

//        AccelerateApplication.application.addActivity(this);

        mFragmentManager = getSupportFragmentManager();

//        initTitle();
    }

    // 适配虚拟按键
    private void setForNavigationBar() {
        int navHeight = DimenUtils.getNavigationHeight(this);
        Log.i("MyLog", "navHeight -> " + navHeight);
        if (navHeight > 0) {
            FrameLayout content = ((FrameLayout) findViewById(android.R.id.content));
            int childCount = content.getChildCount();
            FrameLayout.LayoutParams layoutParams;
            for (int i = 0; i < childCount; i++) {
                View childView = content.getChildAt(i);

//                childView.setPadding(0, 0, 0, navHeight);
                layoutParams = (FrameLayout.LayoutParams) childView.getLayoutParams();
                layoutParams.bottomMargin = navHeight;
            }

            View navBar = new View(this);
            FrameLayout.LayoutParams navBarLayoutParams =
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            navHeight, Gravity.BOTTOM);
            navBar.setBackgroundColor(Color.parseColor("#ff000000"));
            content.addView(navBar, navBarLayoutParams);
        }
    }

    protected abstract int obtainLayoutResID();

    protected abstract String getActivityTitle();

    protected abstract boolean getActivityHasBack();

    protected boolean getActivityHasSetting() {
        return false;
    }

    protected boolean isShowTitle() {
        return true;
    }

    protected int getCustomStyleResourceId() {
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        AccelerateApplication.application.removeActivity(this);

        if (Build.VERSION.SDK_INT >= 16) {
            AccelerateApplication.getRefWatcher().watch(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        ObjectUtil.obtainDynamicPermissionManager().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    public void initTitle() {
        Log.i("MyLog", "initTitle()");
        if (!isShowTitle()) {
            return;
        }
//        if (mTitleLayout == null) {
//            mTitleLayout = (TitleView) findViewById(R.id.title_root_layout);
//        }
        if (mTitleLayout == null) {
            return;
        }
        mTitleLayout.setOnTitleClickListener(this);
        mTitleLayout.setTitle(getActivityTitle());
        if (!getActivityHasBack()) {
            mTitleLayout.setBackBtnVisibility(View.GONE);
        }
        if (!getActivityHasSetting()) {
            mTitleLayout.setSettingBtnVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unused")
    public void setActivityTitle(final String title) {
        if (mTitleLayout != null) {
            mTitleLayout.setTitle(title);
        }
    }

    @Override
    public void onTitleBack() {
        if (getActivityHasBack()) {
            onBackPressed();
        }
    }

    @Override
    public void onTitleSetting() {

    }

    /**
     * 向服务端调接口或本地操作获取数据
     */
    @SuppressWarnings("unused")
    protected void loadData() {

    }

    @SuppressWarnings("unused")
    protected void setSettingBtnResource(int resid) {
        mTitleLayout.setSettingBtnResource(resid);
    }

    /**
     * 要同时实现虚拟按键和沉侵式状态栏，貌似必须是全屏的？
     */
    @SuppressWarnings({"deprecation", "UnusedParameters"})
    @SuppressLint("InlinedApi")
    public void setFillWindow(Activity activity) {
        int flags = 0;
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT == 19 || Build.VERSION.SDK_INT == 20) {
            flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        } else if (Build.VERSION.SDK_INT >= 21) {
//         状态栏全透明
            flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;

////         状态栏半透明
//            flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

            window.setStatusBarColor(mResources.getColor(R.color.title_bg));
//            if ("chm-ul00".equals(Build.MODEL.toLowerCase()) || "huawei p7-l07".equals(Build.MODEL.toLowerCase())) {
//                window.clearFlags(flags);
//                flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//            }
        }
//        window.addFlags(flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(flags);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
        MobclickAgent.onResume(this);
    }

    protected void onStop() {
        super.onStop();
    }
}
