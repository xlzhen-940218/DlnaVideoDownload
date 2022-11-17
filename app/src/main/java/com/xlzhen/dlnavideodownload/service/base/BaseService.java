package com.xlzhen.dlnavideodownload.service.base;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class BaseService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(this.getClass().getSimpleName(),"onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(this.getClass().getSimpleName(),"onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(this.getClass().getSimpleName(),"onConfigurationChanged..."+newConfig.toString());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(this.getClass().getSimpleName(),"onCreate");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(this.getClass().getSimpleName(),"onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(this.getClass().getSimpleName(),"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(this.getClass().getSimpleName(),"onLowMemory");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(this.getClass().getSimpleName(),"onRebind");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(this.getClass().getSimpleName(),"onTaskRemoved");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.i(this.getClass().getSimpleName(),"onTrimMemory");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(this.getClass().getSimpleName(),"onStart");
    }
}
