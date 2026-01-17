package com.example.appwriteandroidtrae;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AppwriteHelper {

    private static final String APPWRITE_ENDPOINT = "https://fra.cloud.appwrite.io/v1";
    private static final String APPWRITE_PROJECT_ID = "680c76af0037a7d23e44";
    private static final String APPWRITE_DATABASE_ID = "680c778b000f055f6409";
    private static final String APPWRITE_SUBSCRIPTION_COLLECTION_ID = "687250d70020221fb26c";

    private static AppwriteHelper instance;

    private final Context context;

    public interface DataCallback<T> {
        void onSuccess(T result);

        void onError(Exception error);
    }

    public static class SubscriptionItem {
        public final String id;
        public final String name;
        public final String site;
        public final int price;
        public final String note;
        public final String account;
        public final long nextDateMillis;

        public SubscriptionItem(
                String id,
                String name,
                String site,
                int price,
                String note,
                String account,
                long nextDateMillis
        ) {
            this.id = id;
            this.name = name;
            this.site = site;
            this.price = price;
            this.note = note;
            this.account = account;
            this.nextDateMillis = nextDateMillis;
        }
    }

    private AppwriteHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized AppwriteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AppwriteHelper(context);
        }
        return instance;
    }

    public void listSubscriptions(final DataCallback<List<SubscriptionItem>> callback) {
        new Thread(() -> {
            try {
                List<SubscriptionItem> items = fetchSubscriptions();
                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public List<SubscriptionItem> listSubscriptionsSync() throws Exception {
        return fetchSubscriptions();
    }

    private List<SubscriptionItem> fetchSubscriptions() throws Exception {
        HttpURLConnection connection = null;
        try {
            String path = "/databases/" + APPWRITE_DATABASE_ID
                    + "/collections/" + APPWRITE_SUBSCRIPTION_COLLECTION_ID
                    + "/documents";
            URL url = new URL(APPWRITE_ENDPOINT + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Appwrite-Project", APPWRITE_PROJECT_ID);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            if (statusCode >= 200 && statusCode < 300) {
                return parseDocuments(responseBuilder.toString());
            } else {
                throw new Exception("HTTP " + statusCode + ": " + responseBuilder);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<SubscriptionItem> parseDocuments(String json) throws JSONException {
        List<SubscriptionItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray documents = root.optJSONArray("documents");
        if (documents == null) {
            return items;
        }
        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String id = document.optString("$id");
            String name = extractName(document, id);
            String site = document.optString("site", "");
            int price = document.has("price") ? document.optInt("price", -1) : -1;
            String note = document.optString("note", "");
            String account = document.optString("account", "");
            long nextDate = extractNextDate(document);
            items.add(new SubscriptionItem(id, name, site, price, note, account, nextDate));
        }
        return items;
    }

    private String extractName(JSONObject document, String fallback) {
        String value = document.optString("name", null);
        if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
            value = fallback;
        }
        return value;
    }

    private long extractNextDate(JSONObject document) {
        String value = document.optString("nextdate", null);
        if (value != null && !value.isEmpty() && !"null".equalsIgnoreCase(value)) {
            Long millis = parseDateToMillis(value);
            if (millis != null) {
                return millis;
            }
        }
        return -1L;
    }

    private Long parseDateToMillis(String value) {
        try {
            Instant instant = Instant.parse(value);
            return instant.toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDate date = LocalDate.parse(value);
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
