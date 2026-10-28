package tv.guard.switcher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class SetTargetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getBooleanExtra("pause", false)) {
            Targets.pause(ctx);
            HomeGuardService svc = HomeGuardService.running();
            if (svc != null) svc.performGlobalAction(HomeGuardService.GLOBAL_ACTION_HOME);
            return;
        }
        String flat = intent.getStringExtra("component");
        ComponentName component = flat == null ? null : ComponentName.unflattenFromString(flat);
        if (component == null) return;
        Targets.set(ctx, component);
        ctx.startActivity(new Intent()
                .setComponent(component)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
}
