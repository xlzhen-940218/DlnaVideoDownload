package com.xlzhen.dlnavideodownload;

import android.app.Application;
import android.os.Environment;

import com.liulishuo.filedownloader.FileDownloader;

import jaygoo.library.m3u8downloader.M3U8DownloaderConfig;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FileDownloader.setupOnApplicationOnCreate(this);
        M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir(String.format("%s/Download/", Environment.getExternalStorageDirectory().getAbsolutePath()));
    }
}
