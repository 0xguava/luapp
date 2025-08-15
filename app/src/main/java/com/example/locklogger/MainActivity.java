package com.example.locklogger;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView logView;

    private final ActivityResultLauncher<String> notifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Even if denied, foreground notification will still show on many devices,
                // but requesting is recommended for Android 13+ consistency.
                refreshLogs();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logView = findViewById(R.id.logView);
        logView.setMovementMethod(new ScrollingMovementMethod());

        Button btnRefresh = findViewById(R.id.btnRefresh);
        Button btnScrollBottom = findViewById(R.id.btnScrollBottom);
        Button btnClear = findViewById(R.id.btnClear);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnBattery = findViewById(R.id.btnBattery);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        btnRefresh.setOnClickListener(v -> refreshLogs());
        btnScrollBottom.setOnClickListener(v -> scrollToBottom());
        btnClear.setOnClickListener(v -> {
            LogUtils.clearLogs(this);
            refreshLogs();
        });
        btnShare.setOnClickListener(v -> shareLogs());
        btnBattery.setOnClickListener(v -> openBatteryOptimizationSettings());
        btnStart.setOnClickListener(v -> startLoggingService());
        btnStop.setOnClickListener(v -> stopService(new Intent(this, LogService.class)));

        // Ask notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Ensure service is running
        startLoggingService();

        refreshLogs();
    }

    private void startLoggingService() {
        Intent svc = new Intent(this, LogService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }
        Toast.makeText(this, "Logging service started", Toast.LENGTH_SHORT).show();
    }

    private void openBatteryOptimizationSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Battery optimization settings not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshLogs() {
        String content = LogUtils.readAllLogs(this);
        logView.setText(content);
        scrollToBottom();
    }

    private void scrollToBottom() {
        logView.post(() -> logView.scrollTo(0, Math.max(0, logView.getLayout() != null ? logView.getLayout().getHeight() : 0)));
    }

    private void shareLogs() {
        Uri uri = LogUtils.getLogFileUri(this);
        if (uri == null) {
            Toast.makeText(this, "No log file to share", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.putExtra(Intent.EXTRA_SUBJECT, "Lock/Unlock/Login Logs");
        share.putExtra(Intent.EXTRA_TEXT, "Lock Logger log attached.");
        startActivity(Intent.createChooser(share, "Share log via"));
    }
}
