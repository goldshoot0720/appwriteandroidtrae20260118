package com.example.appwriteandroidtrae;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class SubscriptionCheckWorker extends Worker {

    private static final String CHANNEL_ID = "subscription_expiry_channel";

    public SubscriptionCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AppwriteHelper helper = AppwriteHelper.getInstance(getApplicationContext());
            List<AppwriteHelper.SubscriptionItem> items = helper.listSubscriptionsSync();
            checkExpiringSubscriptions(items);
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
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
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        createNotificationChannel();

        String title = "訂閱即將到期";
        String content = item.name + " 將在三天內到期";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        int notificationId = item.id.hashCode();
        manager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "訂閱到期提醒";
            String description = "顯示最近到期的訂閱通知";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

