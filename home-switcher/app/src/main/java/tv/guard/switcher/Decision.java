package tv.guard.switcher;

/** When the stock launcher is in front, pull the chosen desktop forward. */
public final class Decision {
    public static final String STOCK_PKG = "com.mitv.tvhome";
    public static final String STOCK_CLASS = "MainActivityUserMode";
    public static final String FALLBACK_PKG = "com.xiaomi.mitv.settings";
    public static final String FALLBACK_CLASS = "FallbackHome";

    private Decision() {}

    public static boolean isStockHome(String pkg, String cls) {
        if (pkg == null || cls == null) return false;
        if (STOCK_PKG.equals(pkg) && cls.endsWith(STOCK_CLASS)) return true;
        return FALLBACK_PKG.equals(pkg) && cls.endsWith(FALLBACK_CLASS);
    }

    public static boolean shouldPull(String pkg, String cls, String targetPkg, long now, long allowUntil) {
        if (targetPkg == null || targetPkg.isEmpty()) return false;
        if (now < allowUntil) return false;
        if (!isStockHome(pkg, cls)) return false;
        return !targetPkg.equals(pkg);
    }
}
