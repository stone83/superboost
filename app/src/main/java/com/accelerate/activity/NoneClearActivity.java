package com.accelerate.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accelerate.utils.CommonUtil;
import com.accelerate.utils.MyAnimationDrawable;
import com.boost.booster.clean.R;

public class NoneClearActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        initMemSize();
        startAnimation();
    }
    private void startAnimation(){
        Handler handler = new Handler();
        ImageView layout = (ImageView)findViewById(R.id.img_bg);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyAnimationDrawable.animateRawManuallyFromXML(R.drawable.anim_list2, layout, new Runnable(){
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

    private void initMemSize(){
        TextView txt = (TextView) findViewById(R.id.mem_size);
        long avaiable = CommonUtil.getAvailableMemory(this);
        long total = CommonUtil.getTotalMemory(this);
        String string = String.format(getString(R.string.none_app_finish), Formatter.formatFileSize(this, avaiable), Formatter.formatFileSize(this, total));
        txt.setText(string);
//        txt.setTypeface(CommonUtil.getSourceTypeFont(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
