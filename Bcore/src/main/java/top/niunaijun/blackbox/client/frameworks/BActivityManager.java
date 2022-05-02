package top.niunaijun.blackbox.client.frameworks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.ClientConfig;
import top.niunaijun.blackbox.entity.UnbindRecord;
import top.niunaijun.blackbox.server.ServiceManager;
import top.niunaijun.blackbox.server.am.IBActivityManagerService;

/**
 * Created by Milk on 4/14/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BActivityManager {
    private static BActivityManager sActivityManager = new BActivityManager();
    private IBActivityManagerService mService;

    public static BActivityManager get() {
        return sActivityManager;
    }

    public ClientConfig initProcess(String packageName, String processName, int userId) {
        try {
            return getService().initProcess(packageName, processName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void restartProcess(String packageName, String processName, int userId) {
        try {
            getService().restartProcess(packageName, processName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startActivity(Intent intent, int userId) {
        try {
            getService().startActivity(intent, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int startActivityAms(int userId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, Bundle options) {
        try {
            return getService().startActivityAms(userId, intent, resolvedType, resultTo, resultWho, requestCode, flags, options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int startActivities(int userId, Intent[] intent, String[] resolvedType, IBinder resultTo, Bundle options) {
        try {
            return getService().startActivities(userId, intent, resolvedType, resultTo, options);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ComponentName startService(Intent intent, String resolvedType, int userId) {
        try {
            return getService().startService(intent, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int stopService(Intent intent, String resolvedType, int userId) {
        try {
            return getService().stopService(intent, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Intent bindService(Intent service, IBinder binder, String resolvedType, int userId) {
        try {
            return getService().bindService(service, binder, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void unbindService(IBinder binder, int userId) {
        try {
            getService().unbindService(binder, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onStartCommand(Intent proxyIntent, int userId) {
        try {
            getService().onStartCommand(proxyIntent, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public UnbindRecord onServiceUnbind(Intent proxyIntent, int userId) {
        try {
            return getService().onServiceUnbind(proxyIntent, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onServiceDestroy(Intent proxyIntent, int userId) {
        try {
            getService().onServiceDestroy(proxyIntent, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public IBinder acquireContentProviderClient(ProviderInfo providerInfo) {
        try {
            return getService().acquireContentProviderClient(providerInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Intent sendBroadcast(Intent intent, String resolvedType, int userId) {
        try {
            return getService().sendBroadcast(intent, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IBinder peekService(Intent intent, String resolvedType, int userId) {
        try {
            return getService().peekService(intent, resolvedType, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onActivityCreated(int taskId, IBinder token, IBinder activityRecord) {
        try {
            getService().onActivityCreated(taskId, token, activityRecord);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResumed(IBinder token) {
        try {
            getService().onActivityResumed(token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onActivityDestroyed(IBinder token) {
        try {
            getService().onActivityDestroyed(token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onFinishActivity(IBinder token) {
        try {
            getService().onFinishActivity(token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private IBActivityManagerService getService() {
        if (mService != null && mService.asBinder().isBinderAlive()) {
            return mService;
        }
        mService = IBActivityManagerService.Stub.asInterface(BlackBoxCore.get().getService(ServiceManager.ACTIVITY_MANAGER));
        return getService();
    }
}
