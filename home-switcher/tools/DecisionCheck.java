public class DecisionCheck {
    public static void main(String[] args) {
        String stock = "com.mitv.tvhome";
        String stockCls = "com.mitv.tvhome.MainActivityUserMode";
        String aerial = "app.orchard.tv";
        check(tv.guard.switcher.Decision.shouldPull(stock, stockCls, aerial, 100, 0), "pull aerial");
        check(!tv.guard.switcher.Decision.shouldPull(stock, stockCls, stock, 100, 0), "stock is the target");
        check(!tv.guard.switcher.Decision.shouldPull("com.google.android.youtube.tv", "a.B", aerial, 100, 0), "leave other apps");
        check(!tv.guard.switcher.Decision.shouldPull(stock, stockCls, aerial, 100, 500), "paused");
        check(tv.guard.switcher.Decision.shouldPull(
                "com.xiaomi.mitv.settings",
                "com.xiaomi.mitv.settings.entry.FallbackHome",
                aerial, 100, 0), "fallback home");
        check(!tv.guard.switcher.Decision.shouldPull(
                "com.xiaomi.mitv.settings",
                "com.xiaomi.mitv.settings.entry.MainActivity",
                aerial, 100, 0), "settings is not home");
        check(!tv.guard.switcher.Decision.shouldPull(null, stockCls, aerial, 100, 0), "null pkg");
        check(!tv.guard.switcher.Decision.shouldPull(stock, stockCls, "", 100, 0), "empty target");
        System.out.println("ok");
    }

    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
    }
}
