package top.niunaijun.blackbox.server;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.client.BClient;
import top.niunaijun.blackbox.client.record.service.ServiceRecord;
import top.niunaijun.blackbox.entity.UnbindRecord;
import top.niunaijun.blackbox.client.stub.record.StubServiceRecord;

import static android.app.Service.START_NOT_STICKY;


/**
 * Created by Milk on 4/1/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ClientServiceManager {
    public static final String TAG = "ClientServiceManager";

    private static ClientServiceManager sServiceManager = new ClientServiceManager();

    private Map<Intent.FilterComparison, ServiceRecord> mService = new HashMap<>();

    public static ClientServiceManager get() {
        return sServiceManager;
    }

    public IBinder onBind(Intent proxyIntent) {
        StubServiceRecord serviceRecord = StubServiceRecord.create(proxyIntent);
        Intent intent = serviceRecord.mServiceIntent;
        ServiceInfo serviceInfo = serviceRecord.mServiceInfo;

        if (intent == null || serviceInfo == null)
            return null;

//        Log.d(TAG, "onBind: " + component.toString());

        Service service = getOrCreateService(intent, serviceInfo);
        intent.setExtrasClassLoader(service.getClassLoader());

        ServiceRecord record = findRecord(intent);
        record.incrementAndGetBindCount(intent);
        if (record.hasBinder(intent)) {
            if (record.isRebind()) {
                service.onRebind(intent);
                record.setRebind(false);
            }
            return record.getBinder(intent);
        }

        try {
            IBinder iBinder = service.onBind(intent);
            record.addBinder(intent, iBinder);
            return iBinder;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public int onStartCommand(Intent proxyIntent, int flags, int startId) {
        StubServiceRecord stubRecord = StubServiceRecord.create(proxyIntent);
        if (stubRecord.mServiceIntent == null || stubRecord.mServiceInfo == null) {
            return START_NOT_STICKY;
        }

//        Log.d(TAG, "onStartCommand: " + component.toString());
        Service service = getOrCreateService(stubRecord.mServiceIntent, stubRecord.mServiceInfo);
        stubRecord.mServiceIntent.setExtrasClassLoader(service.getClassLoader());
        ServiceRecord record = findRecord(stubRecord.mServiceIntent);
        record.setStartId(stubRecord.mStartId);
        try {
            int i = service.onStartCommand(stubRecord.mServiceIntent, flags, stubRecord.mStartId);
            BlackBoxCore.getBActivityManager().onStartCommand(proxyIntent, stubRecord.mUserId);
            return i;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onDestroy();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        mService.clear();
//        Log.d(TAG, "onDestroy: ");
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onConfigurationChanged(newConfig);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
//        Log.d(TAG, "onConfigurationChanged");
    }

    public void onLowMemory() {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onLowMemory();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
//        Log.d(TAG, "onLowMemory");
    }

    public void onTrimMemory(int level) {
        if (mService.size() > 0) {
            for (ServiceRecord record : mService.values()) {
                try {
                    record.getService().onTrimMemory(level);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "onTrimMemory");
    }

    public boolean onUnbind(Intent proxyIntent) {
        StubServiceRecord stubRecord = StubServiceRecord.create(proxyIntent);
        if (stubRecord.mServiceIntent == null || stubRecord.mServiceInfo == null) {
            return false;
        }
        Intent intent = stubRecord.mServiceIntent;

        try {
            UnbindRecord unbindRecord = BlackBoxCore.getBActivityManager().onServiceUnbind(proxyIntent, BClient.getUserId());
            if (unbindRecord == null)
                return false;

            Service service = getOrCreateService(stubRecord.mServiceIntent, stubRecord.mServiceInfo);
            stubRecord.mServiceIntent.setExtrasClassLoader(service.getClassLoader());

            ServiceRecord record = findRecord(intent);

            boolean destroy = unbindRecord.getStartId() == 0;
            if (destroy || record.decreaseConnectionCount(intent)) {
                boolean b = service.onUnbind(intent);
                if (destroy) {
                    service.onDestroy();
                    BlackBoxCore.getBActivityManager().onServiceDestroy(proxyIntent, BClient.getUserId());
                    mService.remove(new Intent.FilterComparison(intent));
                }
                record.setRebind(true);
//                Log.d(TAG, "onUnbind：" + stubRecord.mServiceIntent.getComponent().toString());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public IBinder peekService(Intent intent) {
        ServiceRecord record = findRecord(intent);
        if (record == null) {
            return null;
        }
        return record.getBinder(intent);
    }

    public void stopService(ComponentName componentName) {
//        if (componentName == null)
//            return;
//        Log.d(TAG, "stopService：" + componentName.toString());
//        ServiceRecord record = findRecord(componentName);
//        if (record == null)
//            return;
//        if (record.getService() != null) {
//            boolean destroy = record.getStartId() > 0;
//            try {
//                if (destroy) {
//                    record.getService().onDestroy();
//                }
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//        }
    }

    private ServiceRecord findRecord(Intent intent) {
        return mService.get(new Intent.FilterComparison(intent));
    }

    private Service getOrCreateService(Intent intent, ServiceInfo serviceInfo) {
        ServiceRecord record = findRecord(intent);
        if (record != null && record.getService() != null) {
            return record.getService();
        }
        Service service = BClient.getClient().createService(serviceInfo);
        record = new ServiceRecord();
        record.setService(service);
        mService.put(new Intent.FilterComparison(intent), record);
        return service;
    }
}
