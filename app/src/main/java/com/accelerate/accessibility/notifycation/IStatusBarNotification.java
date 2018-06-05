package com.accelerate.accessibility.notifycation;

import android.app.Notification;

public interface IStatusBarNotification {

    String getPackageName();

    Notification getNotification();
}