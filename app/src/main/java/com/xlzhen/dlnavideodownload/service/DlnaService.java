package com.xlzhen.dlnavideodownload.service;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.xlzhen.dlnavideodownload.MainActivity;
import com.xlzhen.dlnavideodownload.R;

import com.xlzhen.dlnavideodownload.service.base.BaseService;
import com.xlzhen.dlnavideodownload.utils.MimeTypeUtils;
import com.xlzhen.dlnavideodownload.utils.NetWorkUtils;
import com.zxt.dlna.dmr.ZxtMediaRenderer;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;

import java.io.File;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import jaygoo.library.m3u8downloader.M3U8Downloader;
import jaygoo.library.m3u8downloader.OnM3U8DownloadListener;
import jaygoo.library.m3u8downloader.bean.M3U8Task;

public class DlnaService extends BaseService {

    private String hostName, hostAddress;

    public static DlnaService dlnaService;

    private AndroidUpnpService upnpService;

    private NotificationManager manager;
    private Map<String, String> m3u8VideoNameMap;

    private MediaScannerConnection mediaScannerConnection;

    public static void startService(Context context) {
        if (dlnaService == null) {
            Intent intent = new Intent(context, DlnaService.class);
            context.startService(intent);
        }
    }

    private void createNotification(String contentText, String contentTitle, Class<?> cls) {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(this.getClass().getName(), this.getClass().getSimpleName(), NotificationManager.IMPORTANCE_LOW);
        }

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(this.getClass().getName());
        }
        builder.setContentText(contentText);

        builder.setContentTitle(contentTitle);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLargeIcon(Icon.createWithResource(getApplicationContext(), R.mipmap.ic_launcher));
        }
        if (cls != null) {
            Intent intent = new Intent(getApplicationContext(), cls);
            builder.setContentIntent(PendingIntent.getActivity(
                    getApplicationContext(),
                    1,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE));
        }
        Notification notification = null;
        notification = builder.build();

        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        startForeground(1, notification);
    }

    private void updateNotification(String contentText, String contentTitle, int total, int current) {

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(this.getClass().getName());
        }
        builder.setContentText(contentText);
        builder.setProgress(total, current, false);
        builder.setContentTitle(contentTitle);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLargeIcon(Icon.createWithResource(getApplicationContext(), R.mipmap.ic_launcher));
        }

        Notification notification = null;
        notification = builder.build();

        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        startForeground(1, notification);
    }

    public static boolean isRuning() {
        return dlnaService != null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dlnaService = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress deviceAddress = NetWorkUtils.getWifiInetAddress(DlnaService.this);
                hostName = deviceAddress.getHostName();
                hostAddress = deviceAddress.getHostAddress();
            }
        }).start();


        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class), serviceConnection
                , Context.BIND_AUTO_CREATE);

        M3U8Downloader.getInstance().setOnM3U8DownloadListener(new OnM3U8DownloadListener() {
            @Override
            public void onDownloadSuccess(M3U8Task task) {
                super.onDownloadSuccess(task);
                Log.v("success", task.getM3U8().getM3u8FilePath());
                String path = String.format("%s/Download/%s.%s", Environment.getExternalStorageDirectory().getAbsolutePath()
                        , m3u8VideoNameMap.get(task.getUrl()), "mp4");
                FFmpeg.executeAsync(String.format("-i %s -codec copy %s", task.getM3U8().getM3u8FilePath()
                        , path), (executionId, returnCode) -> {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        deleteRecursive(new File(task.getM3U8().getDirFilePath()));
                        completeNotification(path);
                        mediaScannerConnection.scanFile(path, getFileType(path));
                    } else {
                        completeNotification(task.getM3U8().getM3u8FilePath());
                    }
                });
                //completeNotification(task.getM3U8().getM3u8FilePath());
            }

            @Override
            public void onDownloadProgress(M3U8Task task) {
                super.onDownloadProgress(task);
                Log.v("progress", task.getProgress() + "");
                updateNotification(m3u8VideoNameMap.get(task.getUrl()), "M3U8 Downloading...", 100, (int) (task.getProgress() * 100));
            }
        });
        createNotification(getString(R.string.receiver_video_dlna), getString(R.string.app_name), MainActivity.class);

        return super.onStartCommand(intent, flags, startId);
    }
    private String getFileType(String path) {
        try {
            return MimeTypeUtils.getMimeType(path.substring(path.lastIndexOf(".") + 1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
            bitmap.copyPixelsFromBuffer(byteBuffer);
            ZxtMediaRenderer mediaRenderer = new ZxtMediaRenderer(Build.MODEL, Build.MANUFACTURER, hostName
                    , hostAddress, byteBuffer.array(), (url, name1, type) -> new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String path = String.format("%s/Download/%s.%s", Environment.getExternalStorageDirectory().getAbsolutePath()
                            , name1, MimeTypeMap.getFileExtensionFromUrl(url));

                    if (MimeTypeMap.getFileExtensionFromUrl(url).equals("m3u8")) {
                        m3u8VideoNameMap.put(url, name1);
                        M3U8Downloader.getInstance().download(url);
                    } else {
                        downloadNormalVideo(url, path);
                    }
                }
            }));
            upnpService.getRegistry().addDevice(mediaRenderer.getDevice());

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            upnpService = null;
        }
    };

    private void downloadNormalVideo(String url, String path) {
        FileDownloader.getImpl().create(url)
                .setPath(path)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.v("pending", task.getPath());
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        Log.v("connected", task.getPath());
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.v("progress", soFarBytes + " " + totalBytes);
                        updateNotification(task.getPath(), task.getFilename(), totalBytes, soFarBytes);

                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                        Log.v("blockComplete", task.getPath());
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                        Log.v("retry", task.getPath());
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.v("completed", task.getPath());
                        completeNotification(task.getPath());
                        mediaScannerConnection.scanFile(path, getFileType(path));
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.v("paused", task.getPath());
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.v("error", e.getMessage());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.v("warn", task.getPath());
                    }
                }).start();
    }

    private void completeNotification(String path) {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(this.getClass().getName());
        }
        builder.setContentText(getString(R.string.downloaded_open));
        builder.setProgress(100, 100, false);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLargeIcon(Icon.createWithResource(getApplicationContext(), R.mipmap.ic_launcher));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(getApplicationContext()
                , getApplicationContext().getPackageName() + ".provider", new File(path)));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        builder.setContentIntent(PendingIntent.getActivity(
                getApplicationContext(),
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE));
        Notification notification = null;
        notification = builder.build();

        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        startForeground(1, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m3u8VideoNameMap = new HashMap<>();
        mediaScannerConnection = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                Log.i(this.getClass().getSimpleName(), "onMediaScannerConnected");
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.i(this.getClass().getSimpleName(),  "onScanCompleted...path..." + path + "...uri..." + uri);
            }
        });
        mediaScannerConnection.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dlnaService = null;
    }
}