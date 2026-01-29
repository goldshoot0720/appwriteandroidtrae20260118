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
    private static final String APPWRITE_BANK_COLLECTION_ID = "6875df530018459b05b6";
    private static final String APPWRITE_ARTICLE_COLLECTION_ID = "687fdfd70003cf0e3f97";
    private static final String APPWRITE_FOOD_COLLECTION_ID = "6868f512003b1abedb72";
    private static final String APPWRITE_COMMON_ACCOUNT_SITE_COLLECTION_ID = "commonaccount";
    private static final String APPWRITE_COMMON_ACCOUNT_NOTE_COLLECTION_ID = "commonaccountnote";

    private static AppwriteHelper instance;

    private final Context context;

    public interface DataCallback<T> {
        void onSuccess(T result);

        void onError(Exception error);
    }

    public static class SubscriptionItem implements java.io.Serializable {
        public final String id;
        public final String name;
        public final String site;
        public final int price;
        public final String note;
        public final String account;
        public final String bank;
        public final long nextDateMillis;

        public SubscriptionItem(
                String id,
                String name,
                String site,
                int price,
                String note,
                String account,
                String bank,
                long nextDateMillis
        ) {
            this.id = id;
            this.name = name;
            this.site = site;
            this.price = price;
            this.note = note;
            this.account = account;
            this.bank = bank;
            this.nextDateMillis = nextDateMillis;
        }
    }

    public static class BankItem implements java.io.Serializable {
        public final String id;
        public final String name;
        public final int deposit;
        public final String site;
        public final String address;
        public final int withdrawals;
        public final int transfer;
        public final String activity;
        public final String card;
        public final String account;

        public BankItem(
                String id,
                String name,
                int deposit,
                String site,
                String address,
                int withdrawals,
                int transfer,
                String activity,
                String card,
                String account
        ) {
            this.id = id;
            this.name = name;
            this.deposit = deposit;
            this.site = site;
            this.address = address;
            this.withdrawals = withdrawals;
            this.transfer = transfer;
            this.activity = activity;
            this.card = card;
            this.account = account;
        }
    }

    public static class ArticleItem implements java.io.Serializable {
        public final String id;
        public final String title;
        public final String content;
        public final long newDateMillis;
        public final String url1;
        public final String url2;
        public final String url3;
        public final String file1;
        public final String file1type;
        public final String file2;
        public final String file2type;
        public final String file3;
        public final String file3type;
        public final long createdAtMillis;
        public final long updatedAtMillis;

        public ArticleItem(
                String id,
                String title,
                String content,
                long newDateMillis,
                String url1,
                String url2,
                String url3,
                String file1,
                String file1type,
                String file2,
                String file2type,
                String file3,
                String file3type,
                long createdAtMillis,
                long updatedAtMillis
        ) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.newDateMillis = newDateMillis;
            this.url1 = url1;
            this.url2 = url2;
            this.url3 = url3;
            this.file1 = file1;
            this.file1type = file1type;
            this.file2 = file2;
            this.file2type = file2type;
            this.file3 = file3;
            this.file3type = file3type;
            this.createdAtMillis = createdAtMillis;
            this.updatedAtMillis = updatedAtMillis;
        }
    }

    public static class FoodItem implements java.io.Serializable {
        public final String id;
        public final String name;
        public final int amount;
        public final int price;
        public final String shop;
        public final long todateMillis;
        public final String photo;
        public final String photohash;
        public final long createdAtMillis;
        public final long updatedAtMillis;

        public FoodItem(
                String id,
                String name,
                int amount,
                int price,
                String shop,
                long todateMillis,
                String photo,
                String photohash,
                long createdAtMillis,
                long updatedAtMillis
        ) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.shop = shop;
            this.todateMillis = todateMillis;
            this.photo = photo;
            this.photohash = photohash;
            this.createdAtMillis = createdAtMillis;
            this.updatedAtMillis = updatedAtMillis;
        }
    }

    public static class CommonAccountSiteItem implements java.io.Serializable {
        public final String id;
        public final String name;
        public final String[] sites; // site01 to site15
        public final long createdAtMillis;
        public final long updatedAtMillis;

        public CommonAccountSiteItem(
                String id,
                String name,
                String[] sites,
                long createdAtMillis,
                long updatedAtMillis
        ) {
            this.id = id;
            this.name = name;
            this.sites = sites;
            this.createdAtMillis = createdAtMillis;
            this.updatedAtMillis = updatedAtMillis;
        }
    }

    public static class CommonAccountNoteItem implements java.io.Serializable {
        public final String id;
        public final String name;
        public final String[] notes; // note01 to note15
        public final long createdAtMillis;
        public final long updatedAtMillis;

        public CommonAccountNoteItem(
                String id,
                String name,
                String[] notes,
                long createdAtMillis,
                long updatedAtMillis
        ) {
            this.id = id;
            this.name = name;
            this.notes = notes;
            this.createdAtMillis = createdAtMillis;
            this.updatedAtMillis = updatedAtMillis;
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

    public void listBanks(final DataCallback<List<BankItem>> callback) {
        new Thread(() -> {
            try {
                List<BankItem> items = fetchBanks();
                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void listArticles(final DataCallback<List<ArticleItem>> callback) {
        new Thread(() -> {
            try {
                List<ArticleItem> items = fetchArticles();
                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void listFoods(final DataCallback<List<FoodItem>> callback) {
        new Thread(() -> {
            try {
                List<FoodItem> items = fetchFoods();
                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void listCommonAccountSites(final DataCallback<List<CommonAccountSiteItem>> callback) {
        new Thread(() -> {
            try {
                List<CommonAccountSiteItem> items = fetchCommonAccountSites();
                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void listCommonAccountNotes(final DataCallback<List<CommonAccountNoteItem>> callback) {
        new Thread(() -> {
            try {
                List<CommonAccountNoteItem> items = fetchCommonAccountNotes();
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
        return fetchData(APPWRITE_SUBSCRIPTION_COLLECTION_ID, this::parseDocuments);
    }

    private List<BankItem> fetchBanks() throws Exception {
        return fetchData(APPWRITE_BANK_COLLECTION_ID, this::parseBanks);
    }

    private List<ArticleItem> fetchArticles() throws Exception {
        return fetchData(APPWRITE_ARTICLE_COLLECTION_ID, this::parseArticles);
    }

    private List<FoodItem> fetchFoods() throws Exception {
        return fetchData(APPWRITE_FOOD_COLLECTION_ID, this::parseFoods);
    }

    private List<CommonAccountSiteItem> fetchCommonAccountSites() throws Exception {
        return fetchData(APPWRITE_COMMON_ACCOUNT_SITE_COLLECTION_ID, this::parseCommonAccountSites);
    }

    private List<CommonAccountNoteItem> fetchCommonAccountNotes() throws Exception {
        return fetchData(APPWRITE_COMMON_ACCOUNT_NOTE_COLLECTION_ID, this::parseCommonAccountNotes);
    }

    private interface Parser<T> {
        List<T> parse(String json) throws JSONException;
    }

    private <T> List<T> fetchData(String collectionId, Parser<T> parser) throws Exception {
        HttpURLConnection connection = null;
        try {
            String path = "/databases/" + APPWRITE_DATABASE_ID
                    + "/collections/" + collectionId
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
                return parser.parse(responseBuilder.toString());
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
            String bank = document.optString("bank", "");
            long nextDate = extractNextDate(document);
            items.add(new SubscriptionItem(id, name, site, price, note, account, bank, nextDate));
        }
        return items;
    }

    private List<BankItem> parseBanks(String json) throws JSONException {
        List<BankItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray documents = root.optJSONArray("documents");
        if (documents == null) {
            return items;
        }
        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String id = document.optString("$id");
            String name = extractName(document, id);
            int deposit = document.optInt("deposit", 0);
            String site = document.optString("site", "");
            String address = document.optString("address", "");
            int withdrawals = document.optInt("withdrawals", 0);
            int transfer = document.optInt("transfer", 0);
            String activity = document.optString("activity", "");
            String card = document.optString("card", "");
            String account = document.optString("account", "");

            items.add(new BankItem(id, name, deposit, site, address, withdrawals, transfer, activity, card, account));
        }
        return items;
    }

    private List<ArticleItem> parseArticles(String json) throws JSONException {
        List<ArticleItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray documents = root.optJSONArray("documents");
        if (documents == null) {
            return items;
        }
        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String id = document.optString("$id", "");
            String title = document.optString("title", "");
            String content = document.optString("content", "");
            long newDateMillis = extractDateField(document, "newDate");
            String url1 = document.optString("url1", "");
            String url2 = document.optString("url2", "");
            String url3 = document.optString("url3", "");
            String file1 = document.optString("file1", "");
            String file1type = document.optString("file1type", "");
            String file2 = document.optString("file2", "");
            String file2type = document.optString("file2type", "");
            String file3 = document.optString("file3", "");
            String file3type = document.optString("file3type", "");
            long createdAtMillis = extractDateField(document, "$createdAt");
            long updatedAtMillis = extractDateField(document, "$updatedAt");

            items.add(new ArticleItem(
                id, title, content, newDateMillis,
                url1, url2, url3,
                file1, file1type, file2, file2type, file3, file3type,
                createdAtMillis, updatedAtMillis
            ));
        }
        return items;
    }

    private List<FoodItem> parseFoods(String json) throws JSONException {
        List<FoodItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray documents = root.optJSONArray("documents");
        if (documents == null) {
            return items;
        }
        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String id = document.optString("$id", "");
            String name = document.optString("name", "");
            int amount = document.optInt("amount", 0);
            int price = document.optInt("price", 0);
            String shop = document.optString("shop", "");
            long todateMillis = extractDateField(document, "todate");
            String photo = document.optString("photo", "");
            String photohash = document.optString("photohash", "");
            long createdAtMillis = extractDateField(document, "$createdAt");
            long updatedAtMillis = extractDateField(document, "$updatedAt");

            items.add(new FoodItem(
                id, name, amount, price, shop, todateMillis,
                photo, photohash, createdAtMillis, updatedAtMillis
            ));
        }
        return items;
    }

    private List<CommonAccountSiteItem> parseCommonAccountSites(String json) throws JSONException {
        List<CommonAccountSiteItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray documents = root.optJSONArray("documents");
        if (documents == null) {
            return items;
        }
        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String id = document.optString("$id", "");
            String name = document.optString("name", "");
            
            // 提取 site01 到 site15
            String[] sites = new String[15];
            for (int j = 0; j < 15; j++) {
                String fieldName = String.format("site%02d", j + 1);
                sites[j] = document.optString(fieldName, "");
            }
            
            long createdAtMillis = extractDateField(document, "$createdAt");
            long updatedAtMillis = extractDateField(document, "$updatedAt");

            items.add(new CommonAccountSiteItem(id, name, sites, createdAtMillis, updatedAtMillis));
        }
        return items;
    }

    private List<CommonAccountNoteItem> parseCommonAccountNotes(String json) throws JSONException {
        List<CommonAccountNoteItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray documents = root.optJSONArray("documents");
        if (documents == null) {
            return items;
        }
        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String id = document.optString("$id", "");
            String name = document.optString("name", "");
            
            // 提取 note01 到 note15
            String[] notes = new String[15];
            for (int j = 0; j < 15; j++) {
                String fieldName = String.format("note%02d", j + 1);
                notes[j] = document.optString(fieldName, "");
            }
            
            long createdAtMillis = extractDateField(document, "$createdAt");
            long updatedAtMillis = extractDateField(document, "$updatedAt");

            items.add(new CommonAccountNoteItem(id, name, notes, createdAtMillis, updatedAtMillis));
        }
        return items;
    }

    private long extractDateField(JSONObject document, String fieldName) {
        String value = document.optString(fieldName, null);
        if (value != null && !value.isEmpty() && !"null".equalsIgnoreCase(value)) {
            Long millis = parseDateToMillis(value);
            if (millis != null) {
                return millis;
            }
        }
        return -1L;
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
