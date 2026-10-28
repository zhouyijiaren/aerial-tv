package tv.guard.switcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public final class Targets {
    static final String PREFS = "guard";
    static final String KEY = "component";
    static final String ALLOW = "allow_until";
    public static final String AERIAL = "app.orchard.tv/app.orchard.tv.MainActivity";
    public static final long PAUSE_MS = 5 * 60 * 1000L;

    private Targets() {}

    public static ComponentName get(Context ctx) {
        String saved = prefs(ctx).getString(KEY, "");
        if (saved == null || saved.isEmpty()) {
            if (installed(ctx, "app.orchard.tv")) {
                return ComponentName.unflattenFromString(AERIAL);
            }
            return null;
        }
        return ComponentName.unflattenFromString(saved);
    }

    public static void set(Context ctx, ComponentName component) {
        prefs(ctx).edit()
                .putString(KEY, component == null ? "" : component.flattenToString())
                .putLong(ALLOW, 0L)
                .commit();
    }

    public static void pause(Context ctx) {
        prefs(ctx).edit().putLong(ALLOW, System.currentTimeMillis() + PAUSE_MS).commit();
    }

    public static long allowUntil(Context ctx) {
        return prefs(ctx).getLong(ALLOW, 0L);
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static boolean installed(Context ctx, String pkg) {
        try {
            ctx.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
