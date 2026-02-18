package com.example.appwriteandroidtrae;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class FengCommonActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ListView listView;
    private TextView textViewError;
    private CommonAdapter adapter;
    private final List<AppwriteHelper.CommonAccountItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feng_common);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("鋒兄常用");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listViewCommon);
        textViewError = findViewById(R.id.textViewError);

        adapter = new CommonAdapter(this, items);
        listView.setAdapter(adapter);

        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        textViewError.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);

        AppwriteHelper.getInstance(getApplicationContext())
                .listCommonAccounts(new AppwriteHelper.DataCallback<List<AppwriteHelper.CommonAccountItem>>() {
                    @Override
                    public void onSuccess(List<AppwriteHelper.CommonAccountItem> result) {
                        runOnUiThread(() -> {
                            items.clear();
                            items.addAll(result);
                            progressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
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

    private static class CommonAdapter extends ArrayAdapter<AppwriteHelper.CommonAccountItem> {

        CommonAdapter(android.content.Context context, List<AppwriteHelper.CommonAccountItem> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_common_child, parent, false);
            }

            AppwriteHelper.CommonAccountItem item = getItem(position);
            TextView textContent = convertView.findViewById(R.id.textChildContent);

            if (item != null) {
                String display = item.name;
                if (item.note != null && !item.note.isEmpty()) {
                    display += " - " + item.note;
                }
                textContent.setText(display);
            }

            return convertView;
        }
    }
}
