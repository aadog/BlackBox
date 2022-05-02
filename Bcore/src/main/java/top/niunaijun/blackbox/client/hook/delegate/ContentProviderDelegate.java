package top.niunaijun.blackbox.client.hook.delegate;

import android.net.Uri;
import android.os.Build;
import android.os.IInterface;
import android.util.ArrayMap;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mirror.android.app.ActivityThread;
import mirror.android.app.IActivityManager;
import mirror.android.content.ContentProviderHolderOreo;
import mirror.android.providers.Settings;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.client.BClient;
import top.niunaijun.blackbox.client.hook.proxies.context.providers.ContentProviderStub;
import top.niunaijun.blackbox.client.hook.proxies.context.providers.SettingsProviderStub;
import top.niunaijun.blackbox.utils.compat.BuildCompat;

/**
 * Created by Milk on 3/31/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ContentProviderDelegate {
    public static final String TAG = "ContentProviderDelegate";
    private static Set<String> sInjected = new HashSet<>();

    private IInterface mProvider;

    public static void update(Object holder, String auth) {
        IInterface iInterface;
        if (BuildCompat.isOreo()) {
            iInterface = ContentProviderHolderOreo.provider.get(holder);
        } else {
            iInterface = IActivityManager.ContentProviderHolder.provider.get(holder);
        }

        if (iInterface instanceof Proxy)
            return;
//        ContentProviderDelegate contentProviderDelegate = new ContentProviderDelegate(iInterface);
//        contentProviderDelegate.injectHook();
//        ContentProviderHolderOreo.provider.set(holder, (IInterface) contentProviderDelegate.getProxyInvocation());
        IInterface vContentProvider;
        switch (auth) {
            case "settings":
                vContentProvider = new SettingsProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg());
                break;
            default:
                vContentProvider = new ContentProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg());
                break;
        }
        if (BuildCompat.isOreo()) {
            ContentProviderHolderOreo.provider.set(holder, vContentProvider);
        } else {
            IActivityManager.ContentProviderHolder.provider.set(holder, vContentProvider);
        }
    }

    public static void init() {
        clearSettingProvider();

        BlackBoxCore.getContext().getContentResolver().call(Uri.parse("content://settings"), "", null, null);
        Object activityThread = BlackBoxCore.mainThread();
        ArrayMap<Object, Object> map = (ArrayMap<Object, Object>) ActivityThread.mProviderMap.get(activityThread);

        for (Object value : map.values()) {
            String[] mNames = ActivityThread.ProviderClientRecordP.mNames.get(value);
            if (mNames == null || mNames.length <= 0) {
                continue;
            }
            String providerName = mNames[0];
            if (!sInjected.contains(providerName)) {
                sInjected.add(providerName);
                final IInterface iInterface = ActivityThread.ProviderClientRecordP.mProvider.get(value);
//                if (iInterface instanceof Proxy)
//                    continue;
                ActivityThread.ProviderClientRecordP.mProvider.set(value, new ContentProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg()));
                ActivityThread.ProviderClientRecordP.mNames.set(value, new String[]{providerName});
            }
        }
    }

    public static void clearSettingProvider() {
        Object cache;
        cache = Settings.System.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }
        cache = Settings.Secure.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Settings.Global.TYPE != null) {
            cache = Settings.Global.sNameValueCache.get();
            if (cache != null) {
                clearContentProvider(cache);
            }
        }
    }

    private static void clearContentProvider(Object cache) {
        if (BuildCompat.isOreo()) {
            Object holder = Settings.NameValueCacheOreo.mProviderHolder.get(cache);
            if (holder != null) {
                Settings.ContentProviderHolder.mContentProvider.set(holder, null);
            }
        } else {
            Settings.NameValueCache.mContentProvider.set(cache, null);
        }
    }

//    public ContentProviderDelegate(IInterface provider) {
//        super(provider.asBinder());
//        mProvider = provider;
//    }
//
//    @Override
//    protected Object getWho() {
//        return mProvider;
//    }
//
//    @Override
//    protected void inject(Object baseInvocation, Object proxyInvocation) {
//
//    }
//
//    @Override
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        MethodParameterUtils.replaceLastAppPkg(args);
//        return super.invoke(mProvider, method, args);
//    }
//
//    @Override
//    public boolean isBadEnv() {
//        return false;
//    }
}
