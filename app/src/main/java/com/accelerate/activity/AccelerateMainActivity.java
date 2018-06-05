package com.accelerate.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.accelerate.AccelerateApplication;
import com.accelerate.accessibility.AccessibilityHelper;
import com.accelerate.accessibility.services.AccelerateAccessibilityService;
import com.accelerate.adapter.ProcessColumAdapter;
import com.accelerate.constant.ConstantValue;
import com.accelerate.domain.ProcessInfo;
import com.accelerate.service.AccelerateService;
import com.accelerate.utils.CommonUtil;
import com.accelerate.view.RecycleViewDivider;
import com.boost.booster.clean.R;
import com.ccmt.library.lru.LruMap;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.boost.booster.clean.R.id.size;
import static com.boost.booster.clean.R.style.custom_alter_dialog_style;


public class AccelerateMainActivity extends AbstractActivity implements ProcessColumAdapter.OnCheckNotAllNotify{
    public static final String INTENT_START_INSTALLDETAIL = "com.demo.startdetail";
    private AccelerateMainActivity mContext;
    private final static String[] CURRENT_PKG = new String[]{"com.boost.booster.clean"};
    private long mAvailable_start;
    private long mAvailable_end;
    private ProcessColumAdapter mAdapter;
    private CheckBox mCheckBox;
    private boolean mIsChecked = true;
    private final String mPageName = "AccelerateMainActivity";
    private List<ProcessInfo> mList;
    private List<String> mFilter = new ArrayList<String>();
    private long mTotalSize = 0;
    private long mCurrent_select_size = 0;
    private Handler handler = new Handler();
    private int mCount = 0;
    private boolean isFirst = true;
    private boolean mIsRestartAccelerateService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        // 以下代码只在startActivity()方法和finish()方法后调用才有用.
//        overridePendingTransition(R.anim.stand,R.anim.splash);

        // 有的山寨手机每次运行当前应用时,如果上次的辅助功能是打开状态,那么会重新打开辅助功能,
        // 辅助功能类的onServiceConnected()方法会被调用,该方法用到了Handler对象,
        // 这个时候Handler对象可能会没有创建好,所以会抛空指针异常.已经出现过这样的问题,而不是猜测会有这样的问题.
        // 解决办法是在Application类创建Handler对象.
        // 另外在当前位置是用的匿名内部类方式创建Handler对象,所以可能会造成Activity对象的内存泄露.
        AccelerateApplication.getAccelerateApplication().mHandlerAccelerate = new AccelerateHandler(mContext);

        initProcessInfo();
        initClearSize();
        initCountProcess();
        initRecycler();

        if (mList.size() == 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startNoneApp();
                    finish();
                }
            }, 200);
        } else {
            LruMap lruMap = LruMap.getInstance();
            if (lruMap.get(ConstantValue.LRU_IS_RESTART_ACCELERATE_SERVICE) != null) {
                lruMap.remove(ConstantValue.LRU_IS_RESTART_ACCELERATE_SERVICE, false);
                mIsRestartAccelerateService = true;
                mCount = 0;
                AccelerateApplication.application.executeAsyncTask(() -> {
                    while (mAdapter == null) {
                        SystemClock.sleep(100);
                    }
                    AccelerateApplication.application.post(() -> {
                        fetchProcessSelect();
                        Intent intent2 = new Intent(AccelerateMainActivity.this, AccelerateService.class);
                        stopService(intent2);
                        startService(intent2);
                    });
                });
            }
        }
    }

    public static class AccelerateHandler extends Handler {
        private AccelerateMainActivity mContext;

        AccelerateHandler(AccelerateMainActivity mContext) {
            this.mContext = mContext;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mContext.isFirst) {
                mContext.mAvailable_start = CommonUtil.getAvailableMemory(mContext);
                mContext.isFirst = false;
            }
            if (msg.obj != null && msg.obj instanceof CountDownLatch) {
                ((CountDownLatch) msg.obj).countDown();
            }
            mContext.startRunningApp(mContext.mCount++, msg.obj);
        }
    }

    @Override
    protected int obtainLayoutResID() {
        return 0;
    }

    @Override
    protected String getActivityTitle() {
        return mResources.getString(R.string.app_name);
    }

    @Override
    protected boolean getActivityHasBack() {
        return false;
    }

    private void initProcessInfo(){
        mList = CommonUtil.obtainCurrentProcessInfo(this, CURRENT_PKG);
        getTotalSize();
    }

    private long getTotalSize(){
        for (ProcessInfo info:mList) {
            mTotalSize += info.getMemorySize();
        }
        return mTotalSize;
    }

    private void initClearSize(){
        TextView txtview = (TextView)findViewById(size);
        String clear_size = Formatter.formatFileSize(mContext, mTotalSize * 1024);
        String unit = "";
        int posk = clear_size.indexOf('K');
        int posm = clear_size.indexOf('M');
        int posg = clear_size.indexOf('G');
        int posb = clear_size.indexOf('B');
        if(posk > 0){
            unit = clear_size.substring(posk, clear_size.length());
            clear_size = clear_size.substring(0, posk);
        } else if(posm > 0){
            unit = clear_size.substring(posm, clear_size.length());
            clear_size = clear_size.substring(0, posm);
        } else if(posg > 0){
            unit = clear_size.substring(posg, clear_size.length());
            clear_size = clear_size.substring(0, posg);
        } else if(posb > 0){
            unit = clear_size.substring(posb, clear_size.length());
            clear_size = clear_size.substring(0, posb);
        }
        txtview.setText(clear_size);
        TextView size_txtm = (TextView)findViewById(R.id.size_txtm);
        size_txtm.setText(unit);
    }

    private  void initCountProcess(){
        TextView txt = (TextView) findViewById(R.id.running);
        String txt_count = String.format(getString(R.string.running), mList.size());
        txt.setText(txt_count);
//        txt.setTypeface(CommonUtil.getSourceTypeFont(this));
    }

    private long getCurrent_Select_Size(){
        boolean[] flags = mAdapter.getFlags();
        long size = 0;
        for(int i = 0; i < flags.length; i++){
            if(flags[i]){
                size += mList.get(i).getMemorySize();
            }
        }
        return size;
    }
    private boolean isHasChecked(){
        boolean[] flags = mAdapter.getFlags();
        for(int i = 0; i < flags.length; i++){
            if(flags[i]){
                return true;
            }
        }
        return false;
    }
    private void updateAccelerateBtnSizeInfo(){
        Button btn = (Button)findViewById(R.id.btn);
        mCurrent_select_size = getCurrent_Select_Size();
        String acclerate = String.format(getString(R.string.imm_acceler), Formatter.formatFileSize(mContext, mCurrent_select_size * 1024));
        btn.setText(acclerate);
//        btn.setTypeface(CommonUtil.getSourceTypeFont(this));
        if(mList.size() == 0){
            return;
        }
        if(0 == mCurrent_select_size){
            btn.setEnabled(false);
            btn.setClickable(false);
        } else {
            btn.setEnabled(true);
            btn.setClickable(true);
        }
        if(isHasChecked()){
            btn.setBackgroundResource(R.drawable.common_button_selector);
            btn.setTextColor(getResources().getColor(R.color.text_white));
        } else {
            btn.setBackgroundResource(R.drawable.common_button_selector_border);
            btn.setTextColor(getResources().getColor(R.color.all_background));
        }
    }

    private void initAccelerateBtnListener(){
        Button btn = (Button)findViewById(R.id.btn);
        String acclerate = String.format(getString(R.string.imm_acceler), Formatter.formatFileSize(mContext, getCurrent_Select_Size() * 1024));
        btn.setText(acclerate);
//        btn.setTypeface(CommonUtil.getSourceTypeFont(this));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AccessibilityHelper.isAccessibilitySettingsOn(mContext, AccelerateAccessibilityService.class)){
                    if(mList.size() == 0){
//                        startNoneApp();
                    } else {
                        mCount = 0;
                        isFirst = true;
                        AccelerateApplication.getAccelerateApplication().mHandlerAccelerate.sendEmptyMessageDelayed(0, 0);
                    }
                } else {
//                    if(mList.size() == 0){
//                        startNoneApp();
//                        return;
//                    }
                    initDialog();
                }
            }
        });
    }
private CompoundButton.OnCheckedChangeListener checkboxallListener = new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mAdapter.setCheckFlags(isChecked);
        mAdapter.notifyDataSetChanged();
        updateAccelerateBtnSizeInfo();
        fetchProcessSelect();
    }
};
    private void initCheckBoxAll(){
        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        mCheckBox.setOnCheckedChangeListener(checkboxallListener);
        updateAccelerateBtnSizeInfo();
        fetchProcessSelect();
    }

    private void initRecycler(){
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new RecycleViewDivider( mContext, LinearLayoutManager.HORIZONTAL
                , R.drawable.recyclerview_divider));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter = new ProcessColumAdapter(mContext, mList);
                mAdapter.setOnNotifyNotAll(mContext);
                recyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new ProcessColumAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int pos) {

                    }
                });
                initCheckBoxAll();
                initAccelerateBtnListener();
            }
        }, 300);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("appcount", String.valueOf(mList.size()));
        MobclickAgent.onEvent(this, "boost_01_00", map);
    }

    private void startNoneApp(){
        stopService(new Intent(this, AccelerateService.class));
        Intent intent = new Intent();
        intent.setClass(mContext, NoneClearActivity.class);
        startActivity(intent);
    }

    private void initDialog(){
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this, custom_alter_dialog_style);
//        builder.setMessage(getString(R.string.running_assistant));
//        builder.setPositiveButton(getString(R.string.accelerate_hand), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startSettingApp();
//            }
//        });
//        builder.setNegativeButton(getString(R.string.accelerate_auto), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startAccessiblitity();
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();

        LayoutInflater inflater = getLayoutInflater();
         View layout = inflater.inflate(R.layout.custom_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, custom_alter_dialog_style);
        builder.setView(layout);
        builder.setPositiveButton(getString(R.string.accelerate_auto), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAccessiblitity();
                MobclickAgent.onEvent(mContext, "boost_01_60");
            }
        });
        builder.setNegativeButton(getString(R.string.accelerate_hand), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                startSettingApp();
                startService(new Intent(AccelerateMainActivity.this, AccelerateService.class));
                MobclickAgent.onEvent(mContext, "boost_01_61");
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void startSettingApp() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
        startActivity(intent);
    }

    private void startRunningApp(String pkg){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.InstalledAppDetails"));
        intent.setData(Uri.parse("package:" + pkg));
        startActivity(intent);
    }

    private void fetchProcessSelect(){
        mFilter.clear();
        boolean[] flags = mAdapter.getFlags();
        for (int i = 0; i < flags.length; i++){
            if (flags[i]){
                if (mFilter.contains(mList.get(i).getPackageName())){
                    mFilter.remove(mList.get(i).getPackageName());
                }
                mFilter.add(mList.get(i).getPackageName());
            }
        }
    }

    private void startRunningApp(int pos, Object obj) {
        if (mFilter.size() > pos) {
            if (pos < 0) {
                pos = 0;
            }
            String packageName = mFilter.get(pos);
            LruMap lruMap = LruMap.getInstance();
            lruMap.put(ConstantValue.LRU_IS_CLICK, true);
            lruMap.put(ConstantValue.LRU_PACKAGE_NAME, packageName);
            lruMap.put(ConstantValue.LRU_IS_DO_TASKABLE, true);
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.InstalledAppDetails"));
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
            if (mFilter.size() == pos + 1) {
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAvailable_end = CommonUtil.getTotalCleanMemory(mContext, mAvailable_start);
//                        Intent intent2 = new Intent();
//                        intent2.setClass(mContext, AccelerateMainActivity.class);
//                        startActivity(intent2);
//                    }
//                }, 500);
                lruMap.put(ConstantValue.LRU_RUNNABLE, (Runnable) () -> {
                    mAvailable_end = CommonUtil.getTotalCleanMemory(mContext, mAvailable_start);
                    Intent intent2 = new Intent();
                    intent2.setClass(mContext, AccelerateMainActivity.class);
                    startActivity(intent2);
                });
            }
            if (obj != null && obj instanceof Runnable) {
                new Thread((Runnable) obj).start();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mIsRestartAccelerateService) {
            mIsRestartAccelerateService = false;
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent2 = new Intent();
                    intent2.putExtra("available", mCurrent_select_size);
                    intent2.setClass(mContext, CalculateFinishActivity.class);
                    startActivity(intent2);
                    finish();
                }
            }, 1000);
        }
    }

    private void startRunningApp(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.InstalledAppDetails"));
        intent.setData(Uri.parse("package:" + mList.get(0)));
        startActivity(intent);

    }

    private void forceStopPackage(String pkgname, Context context) throws Exception{
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
        method.invoke(am, pkgname);
    }

    private void startAccessiblitity(){
        AccessibilityHelper.openAccessibilityServiceSettings(this);
    }

    private void fetchAndProcess(){
        for (final ProcessInfo info:mList) {
            Message message = Message.obtain();
            message.obj = info.getPackageName();
            handler.sendMessageDelayed(message, 500);
        }
    }

    @Override
    public void onNotifyFalse(boolean[] ischks) {
        for (int i = 0; i < ischks.length; i++){
            if (!ischks[i]){
                mIsChecked = false;
                break;
            } else {
                mIsChecked = true;
            }
        }
        mCheckBox.setOnCheckedChangeListener(null);
        mCheckBox.setChecked(mIsChecked);
        mCheckBox.setOnCheckedChangeListener(checkboxallListener);
        updateAccelerateBtnSizeInfo();
        fetchProcessSelect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LruMap lruMap = LruMap.getInstance();
        Boolean click = (Boolean) lruMap.get(ConstantValue.LRU_IS_CLICK);
        if (click != null) {
            synchronized (AccelerateService.LOCK) {
                lruMap.remove(ConstantValue.LRU_IS_DO_TASKABLE, false);
            }
            mContext.mCount--;
            stopService(new Intent(this, AccelerateService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccelerateApplication.application.mHandlerAccelerate.removeMessages(0);
        AccelerateApplication.application.mHandlerAccelerate.mContext = null;
    }
}
