package top.niunaijun.blackbox.client.hook.proxies.context.providers;

import android.os.IInterface;

/**
 * Created by Milk on 4/8/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public interface VContentProvider {
    IInterface wrapper(final IInterface contentProviderProxy, final String appPkg);
}
