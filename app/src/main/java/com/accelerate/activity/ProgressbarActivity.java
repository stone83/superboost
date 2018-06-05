package com.accelerate.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.accelerate.utils.DialogFractory;
import com.accelerate.utils.LogUtil;
import com.accelerate.utils.ObjectUtil;
import com.accelerate.view.CustomAlertDialog;
import com.accelerate.view.LoadingView;
import com.boost.booster.clean.R;
import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.ViewUtil;

public class ProgressbarActivity extends AbstractActivity {

    private LoadingView mLoadingView = null;
    private boolean mIsSaveInstanceState;
    private boolean mIsInit;
//    private boolean mIsStartRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoadingView = (LoadingView) findViewById(R.id.mImageView);

        makeOutsideClose(false);
    }

    private void makeOutsideClose(boolean outsideClose) {
        if (outsideClose) {
            RelativeLayout root = (RelativeLayout) findViewById(R.id.root);

            // 非透明的内容区域
            View popupWindowView = findViewById(R.id.content_layout);
            root.setOnTouchListener((v, event) -> {
                int x = (int) event.getX();
                int y = (int) event.getY();
                Rect rect = new Rect();
                popupWindowView.getGlobalVisibleRect(rect);
                if (!rect.contains(x, y)) {
                    DialogFractory.closeProgressDialog(ProgressbarActivity.this);
                }
                return false;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LogUtil.i("ProgressbarActivity onDestroy()");

        LruMap lruMap = LruMap.getInstance();
        if (lruMap.get("requestPermissionsRunnable") != null) {
            LogUtil.i("回收Activity占用的内存");

            lruMap.remove("isReturnDialog", false);
            lruMap.remove("isGoToAppSetting", false);
            lruMap.remove("isShowPermissionsDialog", false);
            lruMap.remove("permissionsDialog", false);
            lruMap.remove("requestPermissionsRunnable", false);

//            ObjectUtil.obtainDynamicPermissionManager().setContext(null);
            ObjectUtil.obtainDynamicPermissionManager().reset();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (LruMap.getInstance().get("uninstallSelf") == null) {
//            if (mIsStartRoot) {
//                mIsStartRoot = false;
//
//                LogUtil.i("root授权对话框关闭");
//
//                RootUtil.setStartRootDone(false);
//                ViewUtil.setVisibility(mLoadingView, View.GONE);
//                exit();
//
//                return;
//            }
//        }

        LruMap lruMap = LruMap.getInstance();
        Runnable requestPermissionsRunnable = (Runnable) lruMap.get("requestPermissionsRunnable");
        if (lruMap.get("isShow") != null) {
            if (requestPermissionsRunnable == null) {
                LogUtil.i("打开转圈对话框");

                ViewUtil.setVisibility(mLoadingView, View.VISIBLE);
            } else {
                if (!mIsInit) {
                    mIsInit = true;

                    // 开启新的Activity走动态权限流程
                    ViewUtil.setVisibility(mLoadingView, View.GONE);

                    ObjectUtil.obtainDynamicPermissionManager().setContext(this);

                    requestPermissionsRunnable.run();
                } else {
//                    requestPermissionsRunnable.run();
                    Boolean isShowPermissionsDialog = (Boolean) lruMap.get("isShowPermissionsDialog");
                    if (isShowPermissionsDialog != null) {
                        // 弹出了动态权限对话框
                        Boolean isGoToAppSetting = (Boolean) lruMap.get("isGoToAppSetting");
                        if (isGoToAppSetting != null) {
                            // 弹出了动态权限对话框,且用户点了确认.
                            LogUtil.i("弹出了动态权限对话框,且用户点了确认.");
                            DialogFractory.closeProgressDialog(this);
                        } else {
                            Boolean isReturnDialog = (Boolean) lruMap.get("isReturnDialog");
                            if (isReturnDialog != null) {
                                // 弹出了动态权限对话框,且用户点了取消.
                                LogUtil.i("弹出了动态权限对话框,且用户点了取消.");
                                DialogFractory.closeProgressDialog(this);
                            } else {
                                // 只是弹出了动态权限对话框,用户没有点确认或取消.
                                LogUtil.i("只是弹出了动态权限对话框,用户没有点确认或取消.");
                                CustomAlertDialog permissionsDialog = (CustomAlertDialog) lruMap.get("permissionsDialog");
                                if (permissionsDialog != null) {
                                    permissionsDialog.show();
                                }
                            }
                        }
                    } else {
                        // 没有弹对话框
                        LogUtil.i("没有弹动态权限对话框");
                        DialogFractory.closeProgressDialog(this);
                    }
                }
            }
        } else {
            if (requestPermissionsRunnable == null) {
                LogUtil.i("关闭转圈对话框");

                ViewUtil.setVisibility(mLoadingView, View.GONE);
            }
            exit();
        }
    }

    private void exit() {
        if (!mIsSaveInstanceState) {
            onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (LruMap.getInstance().get("uninstallSelf") == null) {
//            if (RootUtil.isModelStartInit()) {
//                LogUtil.i("root授权对话框弹出");
//                mIsStartRoot = true;
//                RootUtil.setStartRootDone(true);
//            }
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mIsSaveInstanceState) {
            mIsSaveInstanceState = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    @Override
    protected boolean isShowTitle() {
        return false;
    }

    @Override
    protected int obtainLayoutResID() {
        return R.layout.activity_dialog_progressbar;
    }

    @Override
    protected String getActivityTitle() {
        return null;
    }

    @Override
    protected boolean getActivityHasBack() {
        return true;
    }

    @Override
    protected int getCustomStyleResourceId() {
        Boolean isRotate = (Boolean) LruMap.getInstance().get("isRotate");
        if (isRotate != null) {
            return R.style.custom_progressbar_activity;
        }
        return R.style.custom_permissions_activity;
    }

}
