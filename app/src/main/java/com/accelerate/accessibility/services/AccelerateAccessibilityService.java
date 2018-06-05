package com.accelerate.accessibility.services;

import android.content.Intent;

import com.accelerate.AccelerateApplication;
import com.accelerate.accessibility.Config;
import com.umeng.analytics.MobclickAgent;

public class AccelerateAccessibilityService extends AbstractAccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        // 发送广播,已经连接上了.
        sendBroadcast(new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT));

        if (AccelerateApplication.application.mHandlerAccelerate != null) {
            AccelerateApplication.application.mHandlerAccelerate.sendEmptyMessageDelayed(0, 0);
        }
        MobclickAgent.onEvent(this, "boost_01_62");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        boolean onUnbind = super.onUnbind(intent);

        //发送广播,已经断开辅助服务.
        sendBroadcast(new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT));

        return onUnbind;
    }

}