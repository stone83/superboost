package com.ccmt.library.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.ccmt.library.R;
import com.ccmt.library.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationManager implements INotificationManager {

    private static final String FOREIGN_ID = "foreign_id";
    private Context context;
    private List<Integer> allForeignUuids;

    @SuppressWarnings("unchecked")
    public NotificationManager(Context context) {
        this.context = context;
        allForeignUuids = ObjectUtil.obtainObject(context, List.class,
                FOREIGN_ID);
        if (allForeignUuids == null) {
            allForeignUuids = new ArrayList<>();
        }
        if (allForeignUuids.size() > 0) {
            Integer foreignUuid;
            for (int i = 0; i < allForeignUuids.size(); i++) {
                foreignUuid = allForeignUuids.get(i);
                deleteNotification(foreignUuid);
            }
            ObjectUtil.removeObject(context, FOREIGN_ID);
        }
    }

    public Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);

        // 设置标题
        // builder.setContentTitle("通知标题");
        builder.setContentTitle("title");

        // builder.setContentText("这是内容");
        builder.setContentText("text");

        // 图标
        builder.setSmallIcon(R.drawable.ic_launcher);

        // 就是通知打开前在，页面可以看见的提示文字
        // builder.setTicker("一闪，搜索");
        builder.setTicker("ticker");

        // 大文本类型的通知
        // NotificationCompat.BigTextStyle big = new
        // NotificationCompat.BigTextStyle();
        // big.setBigContentTitle("大的标题").bigText("大的内容");

        // 设置builder的样式
        // builder.setStyle(big);

        // 大图片的类型
        // NotificationCompat.BigPictureStyle bigimg = new
        // NotificationCompat.BigPictureStyle();
        // Bitmap b = BitmapFactory.decodeResource(getResources(),
        // R.drawable.ic_launcher);
        // bigimg.bigLargeIcon(b);
        // builder.setStyle(bigimg);

        // 可以显示更多文字
        // NotificationCompat.InboxStyle inboxStyle = new
        // NotificationCompat.InboxStyle();
        // inboxStyle.addLine("一行文本");
        // inboxStyle.addLine("2行文本");
        // inboxStyle.addLine("3行文本");
        // inboxStyle.addLine("4行文本");
        // inboxStyle.addLine("5行文本");
        // builder.setStyle(inboxStyle);

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);

        // 服务来调用
        // NotificationManager manager =
        // (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // manager.notify(1, build);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE
                | Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONLY_ALERT_ONCE;

        return notification;
    }

    public int createUuid() {
        int uuid = UUID.randomUUID().toString().hashCode();
        while (allForeignUuids.contains(uuid)) {
            uuid = UUID.randomUUID().toString().hashCode();
        }
        allForeignUuids.add(uuid);
        ObjectUtil.saveObject(context, List.class, FOREIGN_ID, allForeignUuids);
        return uuid;
    }

    @SuppressWarnings("WeakerAccess")
    public void deleteNotification(int foreignUuid) {
        ((android.app.NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(foreignUuid);
        int indexOf = allForeignUuids.indexOf(foreignUuid);
        if (indexOf >= 0) {
            allForeignUuids.remove(indexOf);
        }
    }

}
