package com.wix.reactnativenotifications.core;

import static com.wix.reactnativenotifications.Defs.LOGTAG;
import static com.wix.reactnativenotifications.Defs.NOTIFICATION_EXTRA_ID;
import static com.wix.reactnativenotifications.Defs.NOTIFICATION_EXTRA_PROPS;
import static com.wix.reactnativenotifications.Defs.NOTIFICATION_SHARED_PREFERENCE_KEY;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotification;

public class ScheduledNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(NOTIFICATION_EXTRA_ID, -1);
        if (id == -1) {
            Log.e(LOGTAG, "Error processing scheduled intent: No ID found.");
            return;
        }

        Log.d(LOGTAG, "Received scheduled notification intent ID = " + id);

        final Bundle notificationBundle = intent.getBundleExtra(NOTIFICATION_EXTRA_PROPS);
        IPushNotification pushNotification = PushNotification.get(context, notificationBundle);
        pushNotification.onPostRequest(id);

        SharedPreferences sharedPreferences = context.getSharedPreferences(NOTIFICATION_SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        String idString = Integer.toString(id);
        if (sharedPreferences.getString(idString, null) != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(idString);
            editor.apply();
        }
    }
}
