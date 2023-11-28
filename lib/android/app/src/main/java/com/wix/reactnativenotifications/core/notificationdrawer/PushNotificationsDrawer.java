package com.wix.reactnativenotifications.core.notificationdrawer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.wix.reactnativenotifications.Defs;
import com.wix.reactnativenotifications.core.AppLaunchHelper;
import com.wix.reactnativenotifications.core.NotificationIntentAdapter;
import com.wix.reactnativenotifications.core.notification.PushNotificationProps;

public class PushNotificationsDrawer implements IPushNotificationsDrawer {

    final protected Context mContext;
    final protected AppLaunchHelper mAppLaunchHelper;
    final protected SharedPreferences mSharedPreferences;

    public static IPushNotificationsDrawer get(Context context) {
        return PushNotificationsDrawer.get(context, new AppLaunchHelper());
    }

    public static IPushNotificationsDrawer get(Context context, AppLaunchHelper appLaunchHelper) {
        final Context appContext = context.getApplicationContext();
        if (appContext instanceof INotificationsDrawerApplication) {
            return ((INotificationsDrawerApplication) appContext).getPushNotificationsDrawer(context, appLaunchHelper);
        }

        return new PushNotificationsDrawer(context, appLaunchHelper);
    }

    protected PushNotificationsDrawer(Context context, AppLaunchHelper appLaunchHelper) {
        mContext = context;
        mAppLaunchHelper = appLaunchHelper;
        mSharedPreferences = context.getSharedPreferences(Defs.NOTIFICATION_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public void onAppInit() {
    }

    @Override
    public void onAppVisible() {
    }

    @Override
    public void onNewActivity(Activity activity) {
    }

    @Override
    public void onNotificationOpened() {
    }

    @Override
    public void onNotificationClearRequest(int id) {
        final PendingIntent pendingIntent = NotificationIntentAdapter.createScheduledNotificationIntent(mContext, new PushNotificationProps(new Bundle()), id);
        final AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);

        String idString = Integer.toString(id);
        if (mSharedPreferences.getString(idString, null) != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(idString);
            editor.apply();
        }
    }

    @Override
    public void onNotificationClearRequest(String tag, int id) {
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(tag, id);
    }

    @Override
    public void onAllNotificationsClearRequest() {
        for (String idString: mSharedPreferences.getAll().keySet()) {
            int id = Integer.parseInt(idString);
            onNotificationClearRequest(id);
        }
    }
}
