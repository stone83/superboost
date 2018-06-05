package com.ccmt.library.util;

import android.content.Context;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.lru.SoftMap;
import com.ccmt.library.manager.NotificationManager;

public class CcmtUtil {

    public static NotificationManager obtainNotificationManager(
            final Context context) {
        Class<NotificationManager> cla = NotificationManager.class;
        return LruMap.getInstance().createOrGetElement(cla.getName(), cla,
                new SoftMap.ICreateObjectAble<NotificationManager>() {
                    @Override
                    public NotificationManager createObject() {
                        return new NotificationManager(context);
                    }
                });
    }

}
