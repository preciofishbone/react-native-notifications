package com.wix.reactnativenotifications.fcm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wix.reactnativenotifications.BuildConfig;
import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotification;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

/**
 * Instance-ID + token refreshing handling service. Contacts the FCM to fetch the updated token.
 *
 * @author amitd
 */
public class FcmInstanceIdListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){
        Bundle bundle = message.toIntent().getExtras();
        if(BuildConfig.DEBUG) Log.d(LOGTAG, "New message from FCM: " + bundle);

        // Prevent Ui threads exception in React Native
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // We trigger the react native loading bundle process to make the react context available in facade
                // This is to support receiving silent notification while app is dead/killed
                final ReactInstanceManager reactInstanceManager = ((ReactApplication) getApplication())
                        .getReactNativeHost()
                        .getReactInstanceManager();
                ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

                if (reactContext != null) {
                    handleReceivedMessage(bundle);
                } else {
                    reactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                        @Override
                        public void onReactContextInitialized(ReactContext reactContext) {
                            handleReceivedMessage(bundle);
                            reactInstanceManager.removeReactInstanceEventListener(this);
                        }
                    });
                    // Main trigger point if context not available
                    if (!reactInstanceManager.hasStartedCreatingInitialContext()) {
                        reactInstanceManager.createReactContextInBackground();
                    }
                }
            }
        });
    }

    private void handleReceivedMessage(Bundle messageBundle) {
        try {
            final IPushNotification notification = PushNotification.get(getApplicationContext(), messageBundle);
            notification.onReceived();
        } catch (IPushNotification.InvalidNotificationException e) {
            // An FCM message, yes - but not the kind we know how to work with.
            if(BuildConfig.DEBUG) Log.v(LOGTAG, "FCM message handling aborted", e);
        }
    }
}
