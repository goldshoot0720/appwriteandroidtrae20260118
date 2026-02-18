package com.example.appwriteandroidtrae;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SubscriptionActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "subscription_expiry_channel_v2";
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    private ProgressBar progressBar;
    private ListView listView;
    private TextView textViewError;
    private LinearLayout bannerContainer;
    private TextView textBannerContent;
    private TextView textSummary;
    private ArrayAdapter<AppwriteHelper.SubscriptionItem> adapter;
    private final List<AppwriteHelper.SubscriptionItem> subscriptionItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("訂閱管理");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listViewSubscriptions);
        textViewError = findViewById(R.id.textViewError);
        bannerContainer = findViewById(R.id.bannerContainer);
        textBannerContent = findViewById(R.id.textBannerContent);
        textSummary = findViewById(R.id.textSummary);

        TextView btnDismiss = findViewById(R.id.btnDismissBanner);
        btnDismiss.setOnClickListener(v -> bannerContainer.setVisibility(View.GONE));

        adapter = new SubscriptionAdapter(this, subscriptionItems);
        listView.setAdapter(adapter);

        createNotificationChannel();
        ensureNotificationPermission();

        loadSubscriptions();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
                            subscriptionItems.clear();
                            subscriptionItems.addAll(result);
                            // 日期排序: 由近到遠，無日期排最後
                            Collections.sort(subscriptionItems, (a, b) -> {
                                if (a.nextDateMillis <= 0 && b.nextDateMillis <= 0)
                                    return 0;
                                if (a.nextDateMillis <= 0)
                                    return 1;
                                if (b.nextDateMillis <= 0)
                                    return -1;
                                return Long.compare(a.nextDateMillis, b.nextDateMillis);
                            });
                            adapter.notifyDataSetChanged();

                            // 更新摘要統計
                            updateSummary(result);
                            // 檢查即將到期並顯示通知
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

    private void updateSummary(List<AppwriteHelper.SubscriptionItem> items) {
        int total = items.size();
        int activeCount = 0;
        for (AppwriteHelper.SubscriptionItem item : items) {
            if (item.continueFlag) {
                activeCount++;
            }
        }
        textSummary.setText("共 " + total + " 筆訂閱 ｜ 續訂中 " + activeCount + " 筆");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "訂閱到期提醒";
            String description = "顯示最近到期的訂閱通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        REQUEST_POST_NOTIFICATIONS);
            }
        }
    }

    private void checkExpiringSubscriptions(List<AppwriteHelper.SubscriptionItem> items) {
        long now = System.currentTimeMillis();
        long threeDaysMillis = 3L * 24L * 60L * 60L * 1000L;
        StringBuilder bannerMessage = new StringBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int expiringCount = 0;

        for (AppwriteHelper.SubscriptionItem item : items) {
            if (item.nextDateMillis <= 0L) {
                continue;
            }
            if (item.nextDateMillis >= now && item.nextDateMillis <= now + threeDaysMillis) {
                long daysLeft = TimeUnit.MILLISECONDS.toDays(item.nextDateMillis - now);
                // 發送系統通知 (每個項目一條)
                showExpiryNotification(item, daysLeft);
                expiringCount++;

                // 組合視窗內通知訊息
                String daysText;
                if (daysLeft <= 0) {
                    daysText = "今天";
                } else if (daysLeft == 1) {
                    daysText = "明天";
                } else if (daysLeft == 2) {
                    daysText = "後天";
                } else {
                    daysText = daysLeft + " 天後";
                }
                String priceText = item.price >= 0 ? String.valueOf(item.price) : "?";
                String currencyText = (item.currency != null && !item.currency.isEmpty()) ? item.currency : "TWD";
                bannerMessage.append("• ").append(item.name)
                        .append("  ").append(daysText).append("扣款")
                        .append("  ").append(priceText).append(" ").append(currencyText)
                        .append("  (").append(fmt.format(new Date(item.nextDateMillis))).append(")")
                        .append("\n");
            }
        }

        // 顯示視窗內通知橫幅
        if (bannerMessage.length() > 0) {
            bannerContainer.setVisibility(View.VISIBLE);
            textBannerContent.setText(bannerMessage.toString().trim());
        } else {
            bannerContainer.setVisibility(View.GONE);
        }
    }

    private void showExpiryNotification(AppwriteHelper.SubscriptionItem item, long daysLeft) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        String daysText;
        if (daysLeft <= 0) {
            daysText = "今天";
        } else if (daysLeft == 1) {
            daysText = "明天";
        } else if (daysLeft == 2) {
            daysText = "後天";
        } else {
            daysText = daysLeft + " 天後";
        }

        String title = item.name + " - " + daysText + "扣款";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateText = format.format(new Date(item.nextDateMillis));
        String priceText = item.price >= 0 ? String.valueOf(item.price) : "?";
        String currencyText = (item.currency != null && !item.currency.isEmpty()) ? item.currency : "TWD";
        String content = dateText + " 扣款 " + priceText + " " + currencyText;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        int notificationId = item.id.hashCode();
        manager.notify(notificationId, builder.build());
    }

    private static class SubscriptionAdapter extends ArrayAdapter<AppwriteHelper.SubscriptionItem> {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SubscriptionAdapter(android.content.Context context, List<AppwriteHelper.SubscriptionItem> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = android.view.LayoutInflater.from(getContext())
                        .inflate(R.layout.item_subscription, parent, false);
            }
            AppwriteHelper.SubscriptionItem item = getItem(position);
            TextView textTitle = convertView.findViewById(R.id.textTitle);
            TextView textSubtitle = convertView.findViewById(R.id.textSubtitle);
            TextView textPrice = convertView.findViewById(R.id.textPrice);
            TextView textAccount = convertView.findViewById(R.id.textAccount);
            TextView textNextDate = convertView.findViewById(R.id.textNextDate);
            TextView textNote = convertView.findViewById(R.id.textNote);
            TextView textDaysLeft = convertView.findViewById(R.id.textDaysLeft);
            View urgencyStripe = convertView.findViewById(R.id.urgencyStripe);
            TextView textContinueStatus = convertView.findViewById(R.id.textContinueStatus);

            if (item != null) {
                // 名稱
                textTitle.setText(item.name != null ? item.name : "");

                // 網站
                if (item.site != null && !item.site.isEmpty()) {
                    textSubtitle.setVisibility(View.VISIBLE);
                    textSubtitle.setText("🌐 " + item.site);
                } else {
                    textSubtitle.setVisibility(View.GONE);
                }

                // 價格
                if (item.price >= 0) {
                    textPrice.setVisibility(View.VISIBLE);
                    String currencyText = (item.currency != null && !item.currency.isEmpty()) ? item.currency : "TWD";
                    textPrice.setText("💰 " + item.price + " " + currencyText);
                } else {
                    textPrice.setVisibility(View.GONE);
                }

                // 帳號
                if (item.account != null && !item.account.isEmpty()) {
                    textAccount.setVisibility(View.VISIBLE);
                    textAccount.setText(item.account);
                } else {
                    textAccount.setVisibility(View.GONE);
                }

                // 下次扣款日 + 倒數
                if (item.nextDateMillis > 0L) {
                    textNextDate.setVisibility(View.VISIBLE);
                    String dateText = dateFormat.format(new Date(item.nextDateMillis));
                    textNextDate.setText("📅 " + dateText);

                    // 計算倒數天數
                    long now = System.currentTimeMillis();
                    long daysLeft = TimeUnit.MILLISECONDS.toDays(item.nextDateMillis - now);

                    if (item.nextDateMillis >= now && daysLeft <= 3) {
                        textDaysLeft.setVisibility(View.VISIBLE);
                        String daysText;
                        int badgeColor;
                        int stripeColor;
                        if (daysLeft <= 0) {
                            daysText = "今天";
                            badgeColor = 0xFFD32F2F; // 深紅
                            stripeColor = 0xFFD32F2F;
                        } else if (daysLeft == 1) {
                            daysText = "明天";
                            badgeColor = 0xFFE53935; // 紅
                            stripeColor = 0xFFE53935;
                        } else if (daysLeft == 2) {
                            daysText = "後天";
                            badgeColor = 0xFFFB8C00; // 橘
                            stripeColor = 0xFFFB8C00;
                        } else {
                            daysText = daysLeft + "天後";
                            badgeColor = 0xFFFDD835; // 黃
                            stripeColor = 0xFFFDD835;
                        }
                        textDaysLeft.setText(daysText);

                        // 動態設定 badge 背景色
                        GradientDrawable badgeBg = new GradientDrawable();
                        badgeBg.setShape(GradientDrawable.RECTANGLE);
                        badgeBg.setCornerRadius(20f);
                        badgeBg.setColor(badgeColor);
                        textDaysLeft.setBackground(badgeBg);
                        if (daysLeft >= 3) {
                            textDaysLeft.setTextColor(0xFF212121);
                        } else {
                            textDaysLeft.setTextColor(Color.WHITE);
                        }

                        urgencyStripe.setBackgroundColor(stripeColor);
                    } else {
                        textDaysLeft.setVisibility(View.GONE);
                        urgencyStripe.setBackgroundColor(0xFFBDBDBD); // 灰色
                    }
                } else {
                    textNextDate.setVisibility(View.GONE);
                    textDaysLeft.setVisibility(View.GONE);
                    urgencyStripe.setBackgroundColor(0xFFE0E0E0);
                }

                // 備註
                if (item.note != null && !item.note.isEmpty()) {
                    textNote.setVisibility(View.VISIBLE);
                    textNote.setText(item.note);
                } else {
                    textNote.setVisibility(View.GONE);
                }

                // 續訂狀態
                textContinueStatus.setVisibility(View.VISIBLE);
                if (item.continueFlag) {
                    textContinueStatus.setText("✅ 續訂中");
                    textContinueStatus.setTextColor(0xFF2E7D32);
                } else {
                    textContinueStatus.setText("⏸ 已停止續訂");
                    textContinueStatus.setTextColor(0xFF9E9E9E);
                }
            }

            return convertView;
        }
    }
}
