package com.accelerate.activity;

import android.content.Intent;
import android.os.Bundle;

import com.accelerate.utils.ThreadManager;
import com.boost.booster.clean.R;

import java.lang.ref.WeakReference;


public class SplashActivity extends AbstractActivity {
    private final String mPageName = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        overridePendingTransition(R.anim.stand,R.anim.splash);
        ThreadManager.postDelayed(ThreadManager.THREAD_UI, new InnerRunnable(this), 1000);
    }
    private static class InnerRunnable implements Runnable {
        private final WeakReference<SplashActivity> mActivity;

        public InnerRunnable(SplashActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            SplashActivity activity = mActivity.get();
            if (activity != null) {
                Intent intent = new Intent(activity, AccelerateMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }
    @Override
    protected int obtainLayoutResID() {
        return 0;
    }

    @Override
    protected String getActivityTitle() {
        return null;
    }

    @Override
    protected boolean getActivityHasBack() {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        //启动界面，屏蔽Back键
        //super.onBackPressed();
    }
}
