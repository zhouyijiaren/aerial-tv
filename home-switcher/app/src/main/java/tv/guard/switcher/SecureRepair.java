package tv.guard.switcher;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

public final class SecureRepair {
    public static final String A11Y =
            "tv.guard.switcher/tv.guard.switcher.HomeGuardService";

    private SecureRepair() {}

    public static boolean enabled(Context ctx) {
        String cur = Settings.Secure.getString(
                ctx.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return cur != null && cur.contains(A11Y);
    }

    /** Append this guard. Leave 小爱 and anything else already in the list. */
    public static boolean ensure(Context ctx) {
        try {
            ContentResolver cr = ctx.getContentResolver();
            String cur = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (cur == null || "null".equals(cur)) cur = "";
            if (!cur.contains(A11Y)) {
                String next = cur.isEmpty() ? A11Y : cur + ":" + A11Y;
                Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, next);
            }
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            Settings.Global.putInt(cr, Settings.Global.ADB_ENABLED, 1);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /** The component can stay listed after the service crashes. Toggle it back on. */
    public static boolean rebind(Context ctx) {
        try {
            ContentResolver cr = ctx.getContentResolver();
            String cur = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (cur == null || "null".equals(cur)) cur = "";
            StringBuilder kept = new StringBuilder();
            for (String part : cur.split(":")) {
                if (part.isEmpty() || part.equals(A11Y)) continue;
                if (kept.length() > 0) kept.append(':');
                kept.append(part);
            }
            Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, kept.toString());
            String next = kept.length() == 0 ? A11Y : kept + ":" + A11Y;
            Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, next);
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
}
