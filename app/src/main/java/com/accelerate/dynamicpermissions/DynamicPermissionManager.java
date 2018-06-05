package com.accelerate.dynamicpermissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.accelerate.AccelerateApplication;
import com.accelerate.dynamicpermissions.callback.PermissionOriginResultCallBack;
import com.accelerate.dynamicpermissions.callback.PermissionResultCallBack;
import com.accelerate.utils.CommonUtil;
import com.accelerate.utils.DialogFractory;
import com.accelerate.utils.LogUtil;
import com.accelerate.view.CustomAlertDialog;
import com.ccmt.library.lru.LruMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicPermissionManager {

    private static final int PERMISSION_GRANTED = 1;
    private static final int PERMISSION_RATIONAL = 2;
    private static final int PERMISSION_DENIED = 3;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private PermissionResultCallBack mPermissionResultCallBack;
    private PermissionOriginResultCallBack mPermissionOriginResultCallBack;
    private Activity mContext;
    private List<PermissionInfo> mPermissionListNeedReq;

    /**
     * 被拒绝的权限列表
     */
    private List<PermissionInfo> mPermissionListDenied;

    /**
     * 被接受的权限列表
     */
    private List<PermissionInfo> mPermissionListAccepted;

    private String[] mPermissions;

    public Activity getContext() {
        if (mContext == null) {
            mContext = CommonUtil.obtainTopActivity();
        }
        return mContext;
    }

    public void setContext(Activity mContext) {
        this.mContext = mContext;
    }

    public void reset() {
        mContext = null;
        mPermissionOriginResultCallBack = null;
        mPermissionResultCallBack = null;
    }

    /**
     * 检查单个权限是否被允许,(当应用第一次安装的时候,不会有rational的值,此时返回均是denied)
     *
     * @param permission The name of the permission being checked.
     * @return DynamicPermissionManager.PERMISSION_GRANTED / PERMISSION_DENIED / PERMISSION_RATIONAL or {@code null}
     * if context is not instanceof Activity.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private int checkSinglePermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return PERMISSION_GRANTED;
        }

        if (ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED) {
            LogUtil.i("经过检测,用户允许了" + permission + "权限.");
            return PERMISSION_GRANTED;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mContext, permission)) {
                LogUtil.i("经过检测,用户拒绝了" + permission + "权限,没有点不再提示.");
                return PERMISSION_RATIONAL;
            } else {
                LogUtil.i("经过检测,用户拒绝了" + permission + "权限,点了不再提示.");
                return PERMISSION_DENIED;
            }
        }
    }

    /**
     * 检查多个权限的状态,不会进行权限的申请.(当应用第一次安装的时候,不会有rational的值,此时返回均是denied)
     *
     * @param permissions The name of the permission being checked.
     * @return Map<String, List<PermissionInfo>> or {@code null}
     * if context is not instanceof Activity or topActivity can not be find
     */
    @SuppressWarnings("JavaDoc")
    private Map<String, List<PermissionInfo>> checkMultiPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return null;
        }

        this.mPermissionListNeedReq = new ArrayList<>();
        this.mPermissionListDenied = new ArrayList<>();
        this.mPermissionListAccepted = new ArrayList<>();

        for (String permission : permissions) {
            switch (checkSinglePermission(permission)) {
                case PERMISSION_GRANTED:
                    mPermissionListAccepted.add(new PermissionInfo(permission));
                    break;
                case PERMISSION_RATIONAL:
                    mPermissionListNeedReq.add(new PermissionInfo(permission));
                    break;
                case PERMISSION_DENIED:
                    mPermissionListDenied.add(new PermissionInfo(permission));
                    break;
                default:
                    break;
            }
        }

        // 暂时用不到
//        HashMap<String, List<PermissionInfo>> map = new HashMap<>();
//        if (!mPermissionListAccepted.isEmpty()) {
//            map.put(PERMISSIONS_ACCEPT, mPermissionListAccepted);
//        }
//        if (!mPermissionListNeedReq.isEmpty()) {
//            map.put(PERMISSIONS_RATIONAL, mPermissionListNeedReq);
//        }
//        if (!mPermissionListDenied.isEmpty()) {
//            map.put(PERMISSIONS_DENIED, mPermissionListDenied);
//        }

        return new HashMap<>();
    }

    /**
     * 请求权限核心方法,不开启新的Activity来申请动态权限.给正在显示的Activity用.
     * 注意,请在主线程调用.
     *
     * @param activity
     * @param permissions
     * @param permissionResultCallBack
     * @param permissionOriginResultCallBack
     */
    @SuppressWarnings("JavaDoc")
    private void request(Activity activity, String[] permissions, PermissionResultCallBack permissionResultCallBack,
                         PermissionOriginResultCallBack permissionOriginResultCallBack) {
        if (!checkSituation(permissions, permissionResultCallBack, permissionOriginResultCallBack)) {
            mPermissionOriginResultCallBack = null;
            mPermissionResultCallBack = null;
            return;
        }

        LogUtil.i("mContext -> " + mContext);

        if (activity != null) {
            mContext = activity;
        } else {
            // 如果外面没有传Activity进来,这里必须获取最顶层的Activity,也就是当前显示的Activity,否则在申请动态权限时如果用户点了拒绝,
            // 然后点返回键再进入app,再申请动态权限,Activity的onRequestPermissionsResult()方法不会被调用.
            mContext = CommonUtil.obtainTopActivity();
            if (mContext == null) {
                // 如果最顶层的Activity也为空,就会打开新的Activity,用新的Activity作为媒介来申请动态权限.
                LruMap lruMap = LruMap.getInstance();
                lruMap.put("requestPermissionsRunnable", (Runnable) () -> doRequest(permissions));
                DialogFractory.showProgressDialog(AccelerateApplication.application, false);

                return;
            }
        }

        doRequest(permissions);
    }

    /**
     * 请求权限核心方法,不开启新的Activity来申请动态权限.给正在显示的Activity用.
     * 注意,请在主线程调用.
     *
     * @param activity
     * @param permissions
     * @param permissionResultCallBack
     */
    @SuppressWarnings("JavaDoc")
    public void request(Activity activity, String[] permissions, PermissionResultCallBack permissionResultCallBack) {
        request(activity, permissions, permissionResultCallBack, null);
    }

    /**
     * 请求权限核心方法,打开新的Activity,用新的Activity作为媒介来申请动态权限.给服务和广播接收者用,
     * 当然也可以给正在显示的Activity用,也就是说,该方法通用.
     * 注意,请在主线程调用.
     *
     * @param permissions
     * @param permissionResultCallBack
     * @param permissionOriginResultCallBack
     */
    @SuppressWarnings("JavaDoc")
    private void request(String[] permissions, PermissionResultCallBack permissionResultCallBack,
                         PermissionOriginResultCallBack permissionOriginResultCallBack) {
        if (!checkSituation(permissions, permissionResultCallBack, permissionOriginResultCallBack)) {
            mPermissionOriginResultCallBack = null;
            mPermissionResultCallBack = null;
            return;
        }

        LogUtil.i("mContext -> " + mContext);

        LruMap.getInstance().put("requestPermissionsRunnable", (Runnable) () -> doRequest(permissions));
        DialogFractory.showProgressDialog(AccelerateApplication.application, false);
    }

    /**
     * 请求权限核心方法,打开新的Activity,用新的Activity作为媒介来申请动态权限.给服务和广播接收者用.
     * 当然也可以给正在显示的Activity用,也就是说,该方法通用.
     * 注意,请在主线程调用.
     *
     * @param permissions
     * @param permissionResultCallBack
     */
    @SuppressWarnings("JavaDoc")
    public void request(String[] permissions, PermissionResultCallBack permissionResultCallBack) {
        request(permissions, permissionResultCallBack, null);
    }

//    /**
//     * 暂时用不上,用来显示所申请的被用户允许,拒绝未点不再提示和拒绝点了不再提示的动态权限信息.
//     * @param activity
//     * @param permissions
//     * @param permissionOriginResultCallBack
//     */
//    @SuppressWarnings("JavaDoc")
//    private void request(Activity activity, String[] permissions, PermissionOriginResultCallBack permissionOriginResultCallBack) {
//        request(activity, permissions, null, permissionOriginResultCallBack);
//    }

    private void doRequest(String[] permissions) {
        LogUtil.i("doRequest()");

        this.mPermissions = permissions;

        if (needToRequest()) {
            // 所申请的权限其中有被用户拒绝过
            LogUtil.i("所申请的权限其中有被用户拒绝过");
            LogUtil.i("Arrays.toString(mPermissions) -> " + Arrays.toString(mPermissions));
            LogUtil.i("mContext -> " + mContext);

            ActivityCompat.requestPermissions(mContext, mPermissions, PERMISSION_REQUEST_CODE);
        } else {
            // 所申请的权限完全被用户允许
            LogUtil.i("所申请的权限完全被用户允许");
            LogUtil.i("mPermissionListAccepted -> " + mPermissionListAccepted);

            onResult(mPermissionListAccepted, mPermissionListNeedReq, mPermissionListDenied);

            if (mPermissionResultCallBack != null) {
                onPermissionGranted(mPermissionListAccepted);
                LogUtil.i("mPermissionResultCallBack -> " + mPermissionResultCallBack);
                mPermissionResultCallBack.onPermissionGranted();
            }

            if (LruMap.getInstance().get("requestPermissionsRunnable") != null) {
                DialogFractory.closeProgressDialog(AccelerateApplication.application);
            } else {
//            mContext = null;
                reset();
            }

        }
    }

    /**
     * 检查环境是否满足申请权限的要求
     *
     * @param permissions
     * @param permissionResultCallBack
     * @param permissionOriginResultCallBack
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private boolean checkSituation(String[] permissions, PermissionResultCallBack permissionResultCallBack,
                                   PermissionOriginResultCallBack permissionOriginResultCallBack) {
//        if (Looper.myLooper() != Looper.getMainLooper()) {
//            throw new RuntimeException("request permission only can run in MainThread!");
//        }

        this.mPermissionResultCallBack = permissionResultCallBack;
        this.mPermissionOriginResultCallBack = permissionOriginResultCallBack;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onResult(toPermissionList(permissions), null, null);

            if (mPermissionResultCallBack != null) {
                mPermissionResultCallBack.onPermissionGranted(permissions);
                mPermissionResultCallBack.onPermissionGranted();
            }

            return false;
        }

        return true;
    }

    /**
     * 检查是否需要申请权限
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc"})
    @TargetApi(Build.VERSION_CODES.M)
    private boolean needToRequest() {
        if (checkMultiPermissions(mPermissions) == null) {
            return false;
        }

        int needReqSize = mPermissionListNeedReq.size();
        int deniedSize = mPermissionListDenied.size();
        if (needReqSize > 0 || deniedSize > 0) {
            mPermissions = new String[needReqSize + deniedSize];
            for (int i = 0; i < needReqSize; i++) {
                mPermissions[i] = mPermissionListNeedReq.get(i).getName();
            }
            for (int i = needReqSize; i < mPermissions.length; i++) {
                mPermissions[i] = mPermissionListDenied.get(i - needReqSize).getName();
            }
            return true;
        }
//        int needReqSize = mPermissionListNeedReq.size();
//        if (needReqSize > 0) {
//            mPermissions = new String[needReqSize];
//            for (int i = 0; i < needReqSize; i++) {
//                mPermissions[i] = mPermissionListNeedReq.get(i).getName();
//            }
//            return true;
//        }
//        return mPermissionListDenied.size() > 0;

        return false;
    }

    /**
     * 申请权限结果返回
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @SuppressWarnings({"JavaDoc", "unchecked"})
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            LogUtil.i("permissions.length -> " + permissions.length);
            LogUtil.i("grantResults.length -> " + grantResults.length);

            mPermissionListAccepted = CommonUtil.listInit(mPermissionListAccepted);
            mPermissionListNeedReq = CommonUtil.listInit(mPermissionListNeedReq);
            mPermissionListDenied = CommonUtil.listInit(mPermissionListDenied);

            boolean isAllGranted = true;

            Activity context = getContext();
            PermissionInfo info;
            for (int i = 0; i < permissions.length; i++) {
                info = new PermissionInfo(permissions[i]);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissions[i])) {
                        // 用户没点不再提示
                        LogUtil.i("用户拒绝了" + info.getName() + "权限,没有点不再提示");

                        CommonUtil.listAdd(mPermissionListNeedReq, info);
                    } else {
                        // 用户点了不再提示
                        LogUtil.i("用户拒绝了" + info.getName() + "权限,点了不再提示");

                        CommonUtil.listAdd(mPermissionListDenied, info);
                    }
                    isAllGranted = false;
                } else {
                    LogUtil.i("用户允许了" + info.getName() + "权限");

                    CommonUtil.listAdd(mPermissionListAccepted, info);
                }
            }

            LruMap lruMap = LruMap.getInstance();
            List<PermissionInfo> permissionListAccepted = (List<PermissionInfo>) lruMap.get("permissionListAccepted");
            if (permissionListAccepted == null) {
                permissionListAccepted = new ArrayList<>();
            }
            List<PermissionInfo> permissionListNeedReq = (List<PermissionInfo>) lruMap.get("permissionListNeedReq");
            if (permissionListNeedReq == null) {
                permissionListNeedReq = new ArrayList<>();
            }
            List<PermissionInfo> permissionListDenied = (List<PermissionInfo>) lruMap.get("permissionListDenied");
            if (permissionListDenied == null) {
                permissionListDenied = new ArrayList<>();
            }

            boolean isChange = false;
            PermissionInfo permissionInfo;
            if (mPermissionListAccepted.size() > 0) {
                for (int i = 0; i < mPermissionListAccepted.size(); i++) {
                    permissionInfo = mPermissionListAccepted.get(i);
                    CommonUtil.listAdd(permissionListAccepted, permissionInfo);
                    CommonUtil.listRemove(permissionListNeedReq, permissionInfo);
                    CommonUtil.listRemove(permissionListDenied, permissionInfo);
                }
                isChange = true;
            }
            if (mPermissionListNeedReq.size() > 0) {
                for (int i = 0; i < mPermissionListNeedReq.size(); i++) {
                    permissionInfo = mPermissionListNeedReq.get(i);
                    CommonUtil.listRemove(permissionListAccepted, permissionInfo);
                    CommonUtil.listAdd(permissionListNeedReq, permissionInfo);
                    CommonUtil.listRemove(permissionListDenied, permissionInfo);
                }
                isChange = true;
            }
            if (mPermissionListDenied.size() > 0) {
                for (int i = 0; i < mPermissionListDenied.size(); i++) {
                    permissionInfo = mPermissionListDenied.get(i);
                    CommonUtil.listRemove(permissionListAccepted, permissionInfo);
                    CommonUtil.listRemove(permissionListNeedReq, permissionInfo);
                    CommonUtil.listAdd(permissionListDenied, permissionInfo);
                }
                isChange = true;
            }
            if (isChange) {
                lruMap.put("permissionListAccepted", permissionListAccepted);
                lruMap.put("permissionListNeedReq", permissionListNeedReq);
                lruMap.put("permissionListDenied", permissionListDenied);
            }

//            mPermissionListNeedReq = permissionListNeedReq;
//            int needReqSize = mPermissionListNeedReq.size();
//            if (needReqSize > 0) {
//                mPermissions = new String[needReqSize];
//                for (int i = 0; i < needReqSize; i++) {
//                    mPermissions[i] = mPermissionListNeedReq.get(i).getName();
//                }
//
//                ActivityCompat.requestPermissions(mContext, mPermissions, PERMISSION_REQUEST_CODE);
//
//                return;
//            }

            LogUtil.i("permissionListAccepted -> " + permissionListAccepted);
            LogUtil.i("permissionListNeedReq -> " + permissionListNeedReq);
            LogUtil.i("permissionListDenied -> " + permissionListDenied);

            mPermissionListAccepted = permissionListAccepted;
            mPermissionListNeedReq = permissionListNeedReq;
            mPermissionListDenied = permissionListDenied;

            Runnable requestPermissionsRunnable = (Runnable) lruMap.get("requestPermissionsRunnable");

            CustomAlertDialog dialog;
            CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(context);
            dialog = builder.setTitle(null)
                    .setMessage("是否开启权限")
//                        .setPositiveButton("确定", (dialogInterface, i) -> CommonUtil.goToAppSetting(getContext()))
                    .setPositiveButton("确定", (dialog1, which) -> {
                        Boolean isGoToAppSetting = (Boolean) lruMap.get("isGoToAppSetting");
                        if (isGoToAppSetting == null) {
                            lruMap.put("isGoToAppSetting", true);
                        }

                        CommonUtil.goToAppSetting(context);
                    })
//                        .setNegativeButton("取消", (dialog12, which) -> DialogFractory.closeProgressDialog(getContext()))
                    .setNegativeButton("取消", (dialog12, which) -> {
                        Boolean isReturnDialog = (Boolean) lruMap.get("isReturnDialog");
                        if (isReturnDialog == null) {
                            lruMap.put("isReturnDialog", true);
                        }

                        if (requestPermissionsRunnable != null) {
                            DialogFractory.closeProgressDialog(AccelerateApplication.application);
                        }
                    })
                    .create();

            int deniedSize = mPermissionListDenied.size();
            if (deniedSize > 0) {
                mPermissions = new String[deniedSize];
                for (int i = 0; i < deniedSize; i++) {
                    mPermissions[i] = mPermissionListDenied.get(i).getName();
                }

                lruMap.put("isShowPermissionsDialog", true);

                if (requestPermissionsRunnable == null) {
                    dialog.show();
                }
            }

            onResult(mPermissionListAccepted, mPermissionListNeedReq, mPermissionListDenied);

            if (mPermissionResultCallBack != null) {
                if (mPermissionListDenied.size() != 0) {
                    onPermissionDenied(mPermissionListDenied);
                    isAllGranted = false;
                }

                if (mPermissionListNeedReq.size() != 0) {
                    onRationalShow(mPermissionListNeedReq);
                    isAllGranted = false;
                }

                if (mPermissionListAccepted.size() != 0) {
                    onPermissionGranted(mPermissionListAccepted);
                }

                if (isAllGranted) {
                    onPermissionGranted();
                }
            }

            lruMap.remove("permissionListAccepted");
            lruMap.remove("permissionListNeedReq");
            lruMap.remove("permissionListDenied");

            if (lruMap.get("requestPermissionsRunnable") == null) {
                lruMap.remove("isReturnDialog");
                lruMap.remove("isGoToAppSetting");
                lruMap.remove("isShowPermissionsDialog");

//                mContext = null;
                reset();
            } else {
                lruMap.put("permissionsDialog", dialog);

                if (deniedSize == 0) {
                    DialogFractory.closeProgressDialog(AccelerateApplication.application);
                }
            }
        }
    }

    /**
     * 返回所有结果的列表list,包括通过的,拒绝的,允许提醒的三个内容,各个list有可能为空
     *
     * @param acceptPermissionList
     * @param needRationalPermissionList
     * @param deniedPermissionList
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private void onResult(List<PermissionInfo> acceptPermissionList,
                          List<PermissionInfo> needRationalPermissionList,
                          List<PermissionInfo> deniedPermissionList) {
        if (mPermissionOriginResultCallBack == null) {
            return;
        }

        mPermissionOriginResultCallBack.onResult(acceptPermissionList, needRationalPermissionList, deniedPermissionList);
    }

    /**
     * 权限被用户许可之后回调的方法
     */
    private void onPermissionGranted() {
        mPermissionResultCallBack.onPermissionGranted();
    }

    private void onPermissionGranted(List<PermissionInfo> list) {
        if (list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }

        mPermissionResultCallBack.onPermissionGranted(permissions);
    }

    /**
     * 权限申请被用户否定之后的回调方法,这个主要是当用户点击否定的同时点击了不在弹出,
     * 那么当再次申请权限,此方法会被调用
     *
     * @param list
     */
    @SuppressWarnings("JavaDoc")
    private void onPermissionDenied(List<PermissionInfo> list) {
        if (list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }

        mPermissionResultCallBack.onPermissionDenied(permissions);
    }

    /**
     * 权限申请被用户否定后的回调方法,这个主要场景是当用户点击了否定,但未点击不在弹出,
     * 那么当再次申请权限的时候,此方法会被调用
     *
     * @param list
     */
    @SuppressWarnings("JavaDoc")
    private void onRationalShow(List<PermissionInfo> list) {
        if (list == null || list.size() == 0) return;

        String[] permissions = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            permissions[i] = list.get(i).getName();
        }
        mPermissionResultCallBack.onRationalShow(permissions);
    }

    /**
     * 将字符串数组转换为PermissionInfoList
     *
     * @param permissions
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private List<PermissionInfo> toPermissionList(String... permissions) {
        List<PermissionInfo> result = new ArrayList<>();
        for (String permission : permissions) {
            result.add(new PermissionInfo(permission));
        }
        return result;
    }

}
