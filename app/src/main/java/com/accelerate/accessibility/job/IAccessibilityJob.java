package com.accelerate.accessibility.job;

import android.view.accessibility.AccessibilityEvent;

import com.accelerate.accessibility.services.AbstractAccessibilityService;
import com.accelerate.accessibility.notifycation.IStatusBarNotification;

public interface IAccessibilityJob {

    String getTargetPackageName();

    void onCreateJob(AbstractAccessibilityService service);

    void onReceiveJob(AbstractAccessibilityService accessibilityService, AccessibilityEvent event);

    void onStopJob();

    void onNotificationPosted(IStatusBarNotification sService);

    boolean isEnable();
}