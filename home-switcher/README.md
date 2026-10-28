# 换桌面

小米电视把主页键锁在系统桌面 `com.mitv.tvhome` 上。当贝、欢喜、Emotn、Aerial 这些应用即使登记了 HOME，设置里改默认桌面也会被系统忽略，按主页仍然打开小米桌面。

这个应用不自己画一套桌面，也不停用、不卸载小米桌面。它只在小米桌面冒出来的时候，把你选定的那个桌面应用拉到前面。主页键会先闪一下小米桌面，然后进入你选的应用。

在小米电视 S55 Mini LED（内部型号 MiTV-MFTR0，Android 14）上写的。其他同样锁死主页键的小米电视可以试，没有逐台验证。

## 和 Aerial 的差别

[Aerial](https://github.com/pengchujin/aerial-tv) 用同一类做法把自己拉回前台，桌面就是 Aerial 自己。这里的守护拉的是你选中的任意一个已安装桌面，可以随时换。代码在本仓库的 `home-switcher` 目录，不改 Aerial 自己的桌面。

## 它做了什么

- **桌面守护**是一个无障碍服务，只听「哪个窗口到了前台」。看到小米桌面 `MainActivityUserMode`（或备用的 FallbackHome）就打开你选的应用。不读遥控器按键，也不看画面上的字。小米桌面有时会再盖上来一次，所以一次主页键会连着再拉几次。
- **通知监听**不读通知内容。待机时系统会关掉第三方应用，守护会一起掉。系统重新绑上这个监听之后，它把守护再打开。
- 选定一个桌面时会立刻取消「留在小米桌面 5 分钟」。那 5 分钟里主页键故意留在小米桌面，所以只打开应用、主页键却不跟着走。
- 列表里只有光标那一行是蓝色。「当前」写在已经绑定的那一项上。

小爱同学如果本来就在无障碍列表里，安装时要保留，不要把整份列表覆盖掉。

## 使用

在应用列表里打开「换桌面」。遥控器上下移动，确定键选定。列表最后一项再往下，是「留在小米桌面 5 分钟」。

## 编译

需要 JDK 17、Android SDK Platform 34 和 Build-Tools 34。

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
gradle assembleDebug
```

安装包在 `app/build/outputs/apk/debug/app-debug.apk`。

## 安装到电视

电视和电脑在同一网络。小米电视打开 ADB：设置 → 关于 → 产品型号上连续按确认，直到进入开发者模式，再到账号与安全里打开 ADB 调试。

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant tv.guard.switcher android.permission.WRITE_SECURE_SETTINGS
adb shell cmd notification allow_listener tv.guard.switcher/tv.guard.switcher.RevivalListener
```

把 `tv.guard.switcher/tv.guard.switcher.HomeGuardService` 追加到

`settings get secure enabled_accessibility_services`

不要删掉已有的小爱。然后确认 `settings put secure accessibility_enabled 1`。
