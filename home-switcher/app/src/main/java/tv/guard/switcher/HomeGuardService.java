package tv.guard.switcher;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class HomeGuardService extends AccessibilityService {
    private static final String TAG = "HomeGuard";
    private static volatile HomeGuardService running;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int attempt;
    private long lastEvent;

    private final Runnable pull = new Runnable() {
        @Override
        public void run() {
            // ponytail: five tries per Home press. The stock desktop often covers the chosen one once.
            if (attempt >= 5) return;
            ComponentName target = Targets.get(HomeGuardService.this);
            String targetPkg = target == null ? "" : target.getPackageName();
            if (!Decision.shouldPull(Decision.STOCK_PKG, Decision.STOCK_CLASS, targetPkg,
                    System.currentTimeMillis(), Targets.allowUntil(HomeGuardService.this))) {
                return;
            }
            attempt++;
            Intent launch = new Intent()
                    .setComponent(target)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            try {
                startActivity(launch);
                Log.i(TAG, "pulled " + target.flattenToString());
            } catch (RuntimeException e) {
                Log.w(TAG, "pull failed: " + e.getMessage());
            }
            handler.postDelayed(this, 400);
        }
    };

    static HomeGuardService running() {
        return running;
    }

    @Override
    protected void onServiceConnected() {
        running = this;
        SecureRepair.ensure(this);
        Log.i(TAG, "connected");
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(pull);
        if (running == this) running = null;
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkgCs = event.getPackageName();
        CharSequence clsCs = event.getClassName();
        if (pkgCs == null || clsCs == null) return;
        ComponentName target = Targets.get(this);
        String targetPkg = target == null ? "" : target.getPackageName();
        if (!Decision.shouldPull(pkgCs.toString(), clsCs.toString(), targetPkg,
                System.currentTimeMillis(), Targets.allowUntil(this))) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - lastEvent > 1_000) attempt = 0;
        lastEvent = now;
        handler.removeCallbacks(pull);
        handler.post(pull);
    }

    @Override
    public void onInterrupt() {}

}
