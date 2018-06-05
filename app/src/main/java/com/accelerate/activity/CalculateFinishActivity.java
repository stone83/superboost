package com.accelerate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.accelerate.utils.MyAnimationDrawable;
import com.boost.booster.clean.R;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

public class CalculateFinishActivity extends AbstractActivity {
    private final String mPageName = "CalculateFinishActivity";
    private final long SIZE_100M = 100 * 1024 * 1024;
    private final long SIZE_200M = 200 * 1024 * 1024;
    private final long SIZE_300M = 300 * 1024 * 1024;
    private final long SIZE_400M = 400 * 1024 * 1024;
    private final long SIZE_500M = 500 * 1024 * 1024;
    private final long SIZE_600M = 600 * 1024 * 1024;
    private final long SIZE_700M = 700 * 1024 * 1024;
    private final long SIZE_800M = 800 * 1024 * 1024;
    private final long SIZE_900M = 900 * 1024 * 1024;
    private final long SIZE_1G = 1024 * 1024 * 1024;
    private final long SIZE_1DOT5G = 1 * 1024 * 1024 * 1024 + 500 * 1024 * 1024;
    private final long SIZE_2G = 2 * 1024 * 1024 * 1024;
    private final long SIZE_3G = 3 * 1024 * 1024 * 1024;
    private final long SIZE_3G_ABOVE = 4 * 1024 * 1024 * 1024;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);
        initView();
        startAnimation();
    }
    private void startAnimation(){
        Handler handler = new Handler();
        ImageView layout = (ImageView)findViewById(R.id.img_bg);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyAnimationDrawable.animateRawManuallyFromXML(R.drawable.anim_list, layout, new Runnable(){
                    @Override
                    public void run() {

                    }
                }, new Runnable(){
                    @Override
                    public void run() {

                    }
                });
            }
        }, 500);
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

    public void initView(){
        TextView txt = (TextView) findViewById(R.id.mem_size);
        long total = getCleanSize(getIntent()) * 1024;
        String size = android.text.format.Formatter.formatFileSize(this, total);
        String string = String.format(getString(R.string.calculate)
                , size);
        txt.setText(string);
//        txt.setTypeface(CommonUtil.getSourceTypeFont(this));
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("app_clean_size", calEvent(total));
        MobclickAgent.onEvent(this, "boost_01_31", map);
    }
    private String calEvent(long cleansize){
        long total = 0;
        if(cleansize > 0 && cleansize <= SIZE_100M){
            total = SIZE_100M;
        } else if (cleansize > SIZE_100M && cleansize <= SIZE_200M){
            total = SIZE_200M;
        } else if (cleansize > SIZE_200M && cleansize <= SIZE_300M){
            total = SIZE_300M;
        } else if (cleansize > SIZE_300M && cleansize <= SIZE_400M){
            total = SIZE_400M;
        } else if (cleansize > SIZE_400M && cleansize <= SIZE_500M){
            total = SIZE_500M;
        } else if (cleansize > SIZE_500M && cleansize <= SIZE_600M){
            total = SIZE_600M;
        } else if (cleansize > SIZE_600M && cleansize <= SIZE_700M){
            total = SIZE_700M;
        } else if (cleansize > SIZE_700M && cleansize <= SIZE_800M){
            total = SIZE_800M;
        } else if (cleansize > SIZE_800M && cleansize <= SIZE_900M){
            total = SIZE_900M;
        } else if (cleansize > SIZE_900M && cleansize <= SIZE_1G){
            total = SIZE_1G;
        } else if(cleansize > SIZE_1G && cleansize <= SIZE_1DOT5G){
            total = SIZE_1DOT5G;
        } else if(cleansize > SIZE_1DOT5G && cleansize <= SIZE_2G){
            total = SIZE_2G;
        } else if(cleansize > SIZE_2G && cleansize <= SIZE_3G){
            total = SIZE_3G;
        } else if(cleansize > SIZE_3G){
            total = SIZE_3G_ABOVE;
        }
        return android.text.format.Formatter.formatFileSize(this,  total);
    }
    public long getCleanSize(Intent intent){
        return  intent.getLongExtra("available", 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
