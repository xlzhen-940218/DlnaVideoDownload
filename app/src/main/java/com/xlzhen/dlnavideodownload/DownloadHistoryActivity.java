package com.xlzhen.dlnavideodownload;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DownloadHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyView;
    private List<File> videoFiles = new ArrayList<>();
    private VideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_history);

        View appBar = findViewById(R.id.app_bar);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        View historyRoot = findViewById(R.id.history_root);
        ViewCompat.setOnApplyWindowInsetsListener(historyRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter();
        recyclerView.setAdapter(adapter);

        loadVideos();
    }

    private void loadVideos() {
        File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null && downloadDir.exists()) {
            File[] files = downloadDir.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".mkv"));
            if (files != null) {
                Collections.addAll(videoFiles, files);
                // Sort by date descending
                Collections.sort(videoFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            }
        }

        if (videoFiles.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            File file = videoFiles.get(position);
            holder.nameText.setText(file.getName());
            holder.sizeText.setText(getString(R.string.video_size, formatFileSize(file.length())));
            holder.dateText.setText(getString(R.string.video_date, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(file.lastModified()))));

            Glide.with(DownloadHistoryActivity.this)
                    .load(file)
                    .centerCrop()
                    .into(holder.thumbnail);

            holder.btnPlay.setOnClickListener(v -> {
                Intent intent = new Intent(DownloadHistoryActivity.this, VideoPlayerActivity.class);
                intent.putExtra("video_path", file.getAbsolutePath());
                startActivity(intent);
            });

            holder.btnSave.setOnClickListener(v -> saveToSystemDownloads(file));
        }

        @Override
        public int getItemCount() {
            return videoFiles.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialTextView nameText, sizeText, dateText;
            MaterialButton btnPlay, btnSave;
            ShapeableImageView thumbnail;

            ViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.video_name);
                sizeText = itemView.findViewById(R.id.video_size);
                dateText = itemView.findViewById(R.id.video_date);
                btnPlay = itemView.findViewById(R.id.btn_play);
                btnSave = itemView.findViewById(R.id.btn_save);
                thumbnail = itemView.findViewById(R.id.video_thumbnail);
            }
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private void saveToSystemDownloads(File sourceFile) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.getName());
                values.put(MediaStore.Downloads.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(uri);
                         InputStream in = new FileInputStream(sourceFile)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), sourceFile.getName());
                try (InputStream in = new FileInputStream(sourceFile);
                     OutputStream out = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    // Scan the file so it appears in the system
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(destFile));
                    sendBroadcast(intent);
                    Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}