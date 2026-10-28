package tv.guard.switcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PickerActivity extends Activity {
    private final List<ComponentName> components = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private TextView status;
    private Button enable;
    private ListView list;
    private boolean kicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker);
        status = findViewById(R.id.status);
        enable = findViewById(R.id.enable);
        list = findViewById(R.id.list);
        loadHomes();
        list.setItemsCanFocus(false);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, labels) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                row.setFocusable(false);
                row.setTextSize(22);
                row.setTextColor(Color.WHITE);
                row.setMinHeight(dp(72));
                int pad = dp(16);
                row.setPadding(pad, pad, pad, pad);
                boolean current = components.get(position).equals(Targets.get(PickerActivity.this));
                String text = labels.get(position);
                if (current) text = "当前 · " + text;
                row.setText(text);
                boolean cursor = position == list.getSelectedItemPosition();
                row.setBackgroundColor(cursor ? 0xFF2F6FED : 0xFF1A1D27);
                return row;
            }
        });
        list.setOnItemClickListener((parent, view, position, id) -> choose(components.get(position), labels.get(position)));
        list.setSelection(0);
        list.requestFocus();
        enable.setOnClickListener(v -> {
            if (SecureRepair.rebind(this)) refresh();
            else Toast.makeText(this, "还不能写系统设置，用电脑授权一次之后就能在这里开", Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.pause).setOnClickListener(v -> {
            Targets.pause(this);
            HomeGuardService svc = HomeGuardService.running();
            if (svc != null) svc.performGlobalAction(HomeGuardService.GLOBAL_ACTION_HOME);
            else Toast.makeText(this, "守护还没连上", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        if (list != null) list.requestFocus();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && list != null && list.getCount() > 0) {
            int key = event.getKeyCode();
            int pos = list.getSelectedItemPosition();
            if (pos < 0) pos = 0;
            if (key == KeyEvent.KEYCODE_DPAD_DOWN && list.hasFocus() && pos < list.getCount() - 1) {
                move(pos + 1);
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_UP && !list.hasFocus()) {
                list.requestFocus();
                move(Math.min(pos, list.getCount() - 1));
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_UP && list.hasFocus() && pos > 0) {
                move(pos - 1);
                return true;
            }
            if ((key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER)
                    && list.hasFocus() && pos < components.size()) {
                choose(components.get(pos), labels.get(pos));
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void move(int position) {
        list.setSelection(position);
        list.invalidateViews();
    }

    private void choose(ComponentName component, String label) {
        Targets.set(this, component);
        String shown = label.split("\n")[0];
        if (Decision.STOCK_PKG.equals(component.getPackageName())) {
            Toast.makeText(this, "主页键将留在" + shown, Toast.LENGTH_SHORT).show();
            refresh();
            return;
        }
        try {
            startActivity(new Intent()
                    .setComponent(component)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            Toast.makeText(this, "主页键将打开 " + shown, Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            Toast.makeText(this, shown + " 打不开", Toast.LENGTH_SHORT).show();
        }
        refresh();
    }

    private void refresh() {
        if (!kicked && SecureRepair.enabled(this) && HomeGuardService.running() == null) {
            kicked = true;
            SecureRepair.rebind(this);
            status.postDelayed(this::refresh, 800);
        }
        ComponentName current = Targets.get(this);
        String name = current == null ? "还没选" : labelOf(current);
        boolean on = HomeGuardService.running() != null;
        boolean paused = System.currentTimeMillis() < Targets.allowUntil(this);
        if (!on) status.setText("守护还没连上。按下面的按钮再开一次。");
        else if (paused) status.setText("这 5 分钟主页键留在小米桌面。");
        else status.setText("守护已开。主页键打开：" + name + "。原桌面会先闪一下。");
        if (list != null) list.invalidateViews();
        enable.setVisibility(on ? View.GONE : View.VISIBLE);
    }

    private void loadHomes() {
        PackageManager pm = getPackageManager();
        Intent probe = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> found = pm.queryIntentActivities(probe, PackageManager.MATCH_ALL);
        Collections.sort(found, Comparator.comparing(info -> info.loadLabel(pm).toString()));
        for (ResolveInfo info : found) {
            if (info.activityInfo == null) continue;
            String pkg = info.activityInfo.packageName;
            if (getPackageName().equals(pkg)) continue;
            if ("com.xiaomi.mitv.settings".equals(pkg)) continue;
            ComponentName component = new ComponentName(pkg, info.activityInfo.name);
            if (components.contains(component)) continue;
            components.add(component);
            labels.add(info.loadLabel(pm) + "\n" + pkg);
        }
    }

    private String labelOf(ComponentName component) {
        int i = components.indexOf(component);
        if (i >= 0) return labels.get(i).split("\n")[0];
        return component.flattenToString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
