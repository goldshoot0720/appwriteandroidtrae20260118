package com.example.appwriteandroidtrae;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 1. 確保背景排程持續執行
            scheduleDailySubscriptionCheck(context);

            // 2. 嘗試啟動 MainActivity
            // 注意：Android 10 (API 29) 以上限制了從背景啟動 Activity，
            // 除非擁有 SYSTEM_ALERT_WINDOW 權限或符合其他條件，否則可能不會直接彈出畫面。
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    private void scheduleDailySubscriptionCheck(Context context) {
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

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "subscription_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }
}
