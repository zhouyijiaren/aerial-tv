package tv.guard.switcher;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/** Rebound by the system after standby kills third-party apps. Does not read notifications. */
public class RevivalListener extends NotificationListenerService {
    @Override
    public void onListenerConnected() {
        boolean ok = SecureRepair.ensure(this);
        Log.i("HomeGuard", "listener connected, guard restored=" + ok);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {}
}
