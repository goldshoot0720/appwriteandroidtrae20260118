package com.example.appwriteandroidtrae;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "subscription_expiry_channel";
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    private ProgressBar progressBar;
    private ListView listView;
    private TextView textViewError;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listViewSubscriptions);
        textViewError = findViewById(R.id.textViewError);

        List<String> items = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        createNotificationChannel();
        ensureNotificationPermission();
        scheduleDailySubscriptionCheck();

        loadSubscriptions();
    }

    private void loadSubscriptions() {
        progressBar.setVisibility(View.VISIBLE);
        textViewError.setVisibility(View.GONE);

        AppwriteHelper.getInstance(getApplicationContext())
                .listSubscriptions(new AppwriteHelper.DataCallback<List<AppwriteHelper.SubscriptionItem>>() {
                    @Override
                    public void onSuccess(List<AppwriteHelper.SubscriptionItem> result) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            List<String> display = new ArrayList<>();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            for (AppwriteHelper.SubscriptionItem item : result) {
                                StringBuilder builder = new StringBuilder();
                                builder.append(item.name);
                                if (item.site != null && !item.site.isEmpty()) {
                                    builder.append(" ");
                                    builder.append(item.site);
                                }
                                if (item.price >= 0) {
                                    builder.append(" 價格: ");
                                    builder.append(item.price);
                                }
                                if (item.account != null && !item.account.isEmpty()) {
                                    builder.append(" 帳號: ");
                                    builder.append(item.account);
                                }
                                if (item.nextDateMillis > 0L) {
                                    String dateText = format.format(new Date(item.nextDateMillis));
                                    builder.append(" 下次扣款日: ");
                                    builder.append(dateText);
                                }
                                if (item.note != null && !item.note.isEmpty()) {
                                    builder.append(" 備註: ");
                                    builder.append(item.note);
                                }
                                display.add(builder.toString());
                            }
                            adapter.clear();
                            adapter.addAll(display);
                            adapter.notifyDataSetChanged();
                            checkExpiringSubscriptions(result);
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            textViewError.setVisibility(View.VISIBLE);
                            textViewError.setText("載入失敗: " + error.getMessage());
                        });
                    }
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "訂閱到期提醒";
            String description = "顯示最近到期的訂閱通知";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS
                );
            }
        }
    }

    private void scheduleDailySubscriptionCheck() {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long firstRun = calendar.getTimeInMillis();
        if (firstRun <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            firstRun = calendar.getTimeInMillis();
        }
        long initialDelay = firstRun - now;

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                SubscriptionCheckWorker.class,
                24, TimeUnit.HOURS
        )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                "subscription_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void checkExpiringSubscriptions(List<AppwriteHelper.SubscriptionItem> items) {
        long now = System.currentTimeMillis();
        long threeDaysMillis = 3L * 24L * 60L * 60L * 1000L;
        for (AppwriteHelper.SubscriptionItem item : items) {
            if (item.nextDateMillis <= 0L) {
                continue;
            }
            if (item.nextDateMillis >= now && item.nextDateMillis <= now + threeDaysMillis) {
                showExpiryNotification(item);
            }
        }
    }

    private void showExpiryNotification(AppwriteHelper.SubscriptionItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        String title = "訂閱即將到期";
        String content = item.name + " 將在三天內到期";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        int notificationId = item.id.hashCode();
        manager.notify(notificationId, builder.build());
    }
}
