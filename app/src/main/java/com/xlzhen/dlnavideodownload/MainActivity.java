package com.xlzhen.dlnavideodownload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.xlzhen.dlnavideodownload.service.DlnaService;
import com.xlzhen.dlnavideodownload.utils.NetWorkUtils;

import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    private TextView statusTextView;
    private TextView ipAddressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View appBar = findViewById(R.id.app_bar);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        
        View mainRoot = findViewById(R.id.main_root);
        ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        statusTextView = findViewById(R.id.status_text_view);
        ipAddressTextView = findViewById(R.id.ip_address_text_view);

        findViewById(R.id.card_history).setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadHistoryActivity.class));
        });

        updateIpAddress();

        if (!hasPermissions()) {
            statusTextView.setText(R.string.request_permission);
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.status_error));
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), 100);
        } else {
            startDlnaService();
        }
    }

    private void updateIpAddress() {
        InetAddress address = NetWorkUtils.getWifiInetAddress(this);
        if (address != null) {
            ipAddressTextView.setText(getString(R.string.ip_address_format, address.getHostAddress()));
        } else {
            ipAddressTextView.setText(R.string.ip_address_placeholder);
        }
    }

    private void startDlnaService() {
        DlnaService.startService(this);
        statusTextView.setText(R.string.status_running);
        statusTextView.setTextColor(ContextCompat.getColor(this, R.color.status_running));
    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            return new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    private boolean hasPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (hasPermissions()) {
            startDlnaService();
        } else {
            statusTextView.setText(R.string.request_permission);
            statusTextView.setTextColor(ContextCompat.getColor(this, R.color.status_error));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIpAddress();
    }
}