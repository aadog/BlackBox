package top.niunaijun.blackbox.client.hook.proxies.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import mirror.android.app.ActivityManagerNative;
import mirror.android.app.ActivityThread;
import mirror.android.app.IActivityManager;
import mirror.android.app.servertransaction.ClientTransaction;
import mirror.android.app.servertransaction.LaunchActivityItem;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.client.BClient;
import top.niunaijun.blackbox.client.hook.IInjectHook;
import top.niunaijun.blackbox.client.stub.record.StubActivityRecord;
import top.niunaijun.blackbox.utils.compat.BuildCompat;


/**
 * Created by Milk on 3/31/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class HCallbackStub implements IInjectHook, Handler.Callback {
    public static final String TAG = "HCallbackStub";
    private Handler.Callback mOtherCallback;
    private AtomicBoolean mBeing = new AtomicBoolean(false);

    private Handler.Callback getHCallback() {
        return mirror.android.os.Handler.mCallback.get(getH());
    }

    private Handler getH() {
        Object currentActivityThread = BlackBoxCore.mainThread();
        return ActivityThread.mH.get(currentActivityThread);
    }

    @Override
    public void injectHook() {
        mOtherCallback = getHCallback();
        if (mOtherCallback != null && (mOtherCallback == this || mOtherCallback.getClass().getName().equals(this.getClass().getName()))) {
            mOtherCallback = null;
        }
        mirror.android.os.Handler.mCallback.set(getH(), this);
    }

    @Override
    public boolean isBadEnv() {
        Handler.Callback hCallback = getHCallback();
        return hCallback != null && hCallback != this;
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (!mBeing.getAndSet(true)) {
            try {
                if (BuildCompat.isPie()) {
                    if (msg.what == ActivityThread.H.EXECUTE_TRANSACTION.get()) {
                        if (handleLaunchActivity(msg.obj)) {
                            getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
                            return true;
                        }
                    }
                } else {
                    if (msg.what == ActivityThread.H.LAUNCH_ACTIVITY.get()) {
                        if (handleLaunchActivity(msg.obj)) {
                            getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
                            return true;
                        }
                    }
                }
                if (mOtherCallback != null) {
                    return mOtherCallback.handleMessage(msg);
                }
                return false;
            } finally {
                mBeing.set(false);
            }
        }
        return false;
    }

    private Object getLaunchActivityItem(Object clientTransaction) {
        List<Object> mActivityCallbacks = ClientTransaction.mActivityCallbacks.get(clientTransaction);

        for (Object obj : mActivityCallbacks) {
            if (LaunchActivityItem.TYPE.getName().equals(obj.getClass().getCanonicalName())) {
                return obj;
            }
        }
        return null;
    }

    private boolean handleLaunchActivity(Object client) {
        Object r;
        if (BuildCompat.isPie()) {
            // ClientTransaction
            r = getLaunchActivityItem(client);
        } else {
            // ActivityClientRecord
            r = client;
        }
        if (r == null)
            return false;

        Intent intent;
        IBinder token;
        if (BuildCompat.isPie()) {
            intent = LaunchActivityItem.mIntent.get(r);
            token = ClientTransaction.mActivityToken.get(client);
        } else {
            intent = ActivityThread.ActivityClientRecord.intent.get(r);
            token = ActivityThread.ActivityClientRecord.token.get(r);
        }

        if (intent == null)
            return false;

        StubActivityRecord stubRecord = StubActivityRecord.create(intent);
        ActivityInfo activityInfo = stubRecord.mActivityInfo;
        if (activityInfo != null) {
            if (BClient.getClientConfig() == null) {
                BlackBoxCore.getBActivityManager().restartProcess(activityInfo.packageName, activityInfo.processName, stubRecord.mUserId);
                return true;
            }
            // bind
            if (!BClient.getClient().isInit()) {
                BClient.getClient().bindApplication(activityInfo.packageName,
                        activityInfo.processName);
                return true;
            }

            int taskId = IActivityManager.getTaskForActivity.call(ActivityManagerNative.getDefault.call(), token, false);
            BlackBoxCore.getBActivityManager().onActivityCreated(taskId, token, stubRecord.mActivityRecord);

            if (BuildCompat.isPie()) {
                LaunchActivityItem.mIntent.set(r, stubRecord.mTarget);
                LaunchActivityItem.mInfo.set(r, activityInfo);
            } else {
                ActivityThread.ActivityClientRecord.intent.set(r, stubRecord.mTarget);
                ActivityThread.ActivityClientRecord.activityInfo.set(r, activityInfo);
            }
        }
        return false;
    }
}
