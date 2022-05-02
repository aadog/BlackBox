package top.niunaijun.blackbox.client.hook.env;

import android.content.ComponentName;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;

/**
 * Created by Milk on 4/21/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ClientSystemEnv {
    private static final List<String> sSystemPackages = new ArrayList<>();
    private static final List<String> sSuPackages = new ArrayList<>();
    private static final List<String> sXposedPackages = new ArrayList<>();
    private static final List<String> sPreInstallPackages = new ArrayList<>();

    static {
        sSystemPackages.add("android");
        sSystemPackages.add("com.google.android.webview");
        sSystemPackages.add("com.google.android.webview.dev");
        sSystemPackages.add("com.google.android.webview.beta");
        sSystemPackages.add("com.google.android.webview.canary");
        sSystemPackages.add("com.android.webview");
        sSystemPackages.add("com.android.camera");

        // 华为
        sSystemPackages.add("com.huawei.webview");

        // su
        sSuPackages.add("com.noshufou.android.su");
        sSuPackages.add("com.noshufou.android.su.elite");
        sSuPackages.add("eu.chainfire.supersu");
        sSuPackages.add("com.koushikdutta.superuser");
        sSuPackages.add("com.thirdparty.superuser");
        sSuPackages.add("com.yellowes.su");

        sXposedPackages.add("de.robv.android.xposed.installer");

        sPreInstallPackages.add("com.huawei.hwid");
    }

    public static boolean isOpenPackage(String packageName) {
        return sSystemPackages.contains(packageName);
    }

    public static boolean isOpenPackage(ComponentName componentName) {
        return componentName != null && isOpenPackage(componentName.getPackageName());
    }

    public static boolean isBlackPackage(String packageName) {
        if (BlackBoxCore.get().isHideRoot() && sSuPackages.contains(packageName)) {
            return true;
        } else if (BlackBoxCore.get().isHideXposed() && sXposedPackages.contains(packageName)) {
            return true;
        }
        return false;
    }

    public static List<String> getPreInstallPackages() {
        return sPreInstallPackages;
    }
}
