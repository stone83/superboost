package com.ccmt.library.manager;

import android.app.Notification;

public interface INotificationManager {

    int createUuid();

    Notification createNotification();

}
