package com.example.appwriteandroidtrae;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class FengCommonActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ExpandableListView expandableListView;
    private TextView textViewError;
    private CommonDataAdapter adapter;
    private final List<AppwriteHelper.CommonAccountSiteItem> sites = new ArrayList<>();
    private final List<AppwriteHelper.CommonAccountNoteItem> notes = new ArrayList<>();

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
        expandableListView = findViewById(R.id.expandableListView);
        textViewError = findViewById(R.id.textViewError);

        adapter = new CommonDataAdapter();
        expandableListView.setAdapter(adapter);

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
        expandableListView.setVisibility(View.GONE);

        // 載入 Sites
        AppwriteHelper.getInstance(getApplicationContext())
                .listCommonAccountSites(new AppwriteHelper.DataCallback<List<AppwriteHelper.CommonAccountSiteItem>>() {
                    @Override
                    public void onSuccess(List<AppwriteHelper.CommonAccountSiteItem> result) {
                        runOnUiThread(() -> {
                            sites.clear();
                            sites.addAll(result);
                            checkLoadComplete();
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        runOnUiThread(() -> showError("載入 Sites 失敗: " + error.getMessage()));
                    }
                });

        // 載入 Notes
        AppwriteHelper.getInstance(getApplicationContext())
                .listCommonAccountNotes(new AppwriteHelper.DataCallback<List<AppwriteHelper.CommonAccountNoteItem>>() {
                    @Override
                    public void onSuccess(List<AppwriteHelper.CommonAccountNoteItem> result) {
                        runOnUiThread(() -> {
                            notes.clear();
                            notes.addAll(result);
                            checkLoadComplete();
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        runOnUiThread(() -> showError("載入 Notes 失敗: " + error.getMessage()));
                    }
                });
    }

    private void checkLoadComplete() {
        // 簡單檢查：如果有任何數據就顯示
        if (!sites.isEmpty() || !notes.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            expandableListView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        textViewError.setVisibility(View.VISIBLE);
        textViewError.setText(message);
    }

    private class CommonDataAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return sites.size() + notes.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (groupPosition < sites.size()) {
                // Site item - 計算非空的 sites
                AppwriteHelper.CommonAccountSiteItem item = sites.get(groupPosition);
                int count = 0;
                for (String site : item.sites) {
                    if (site != null && !site.isEmpty()) count++;
                }
                return count;
            } else {
                // Note item - 計算非空的 notes
                AppwriteHelper.CommonAccountNoteItem item = notes.get(groupPosition - sites.size());
                int count = 0;
                for (String note : item.notes) {
                    if (note != null && !note.isEmpty()) count++;
                }
                return count;
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            if (groupPosition < sites.size()) {
                return sites.get(groupPosition);
            } else {
                return notes.get(groupPosition - sites.size());
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if (groupPosition < sites.size()) {
                AppwriteHelper.CommonAccountSiteItem item = sites.get(groupPosition);
                int currentIndex = 0;
                for (String site : item.sites) {
                    if (site != null && !site.isEmpty()) {
                        if (currentIndex == childPosition) return site;
                        currentIndex++;
                    }
                }
            } else {
                AppwriteHelper.CommonAccountNoteItem item = notes.get(groupPosition - sites.size());
                int currentIndex = 0;
                for (String note : item.notes) {
                    if (note != null && !note.isEmpty()) {
                        if (currentIndex == childPosition) return note;
                        currentIndex++;
                    }
                }
            }
            return "";
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(FengCommonActivity.this)
                        .inflate(R.layout.item_common_group, parent, false);
            }

            TextView textName = convertView.findViewById(R.id.textGroupName);
            TextView textType = convertView.findViewById(R.id.textGroupType);

            if (groupPosition < sites.size()) {
                AppwriteHelper.CommonAccountSiteItem item = sites.get(groupPosition);
                textName.setText(item.name != null && !item.name.isEmpty() ? item.name : "未命名");
                textType.setText("網站");
            } else {
                AppwriteHelper.CommonAccountNoteItem item = notes.get(groupPosition - sites.size());
                textName.setText(item.name != null && !item.name.isEmpty() ? item.name : "未命名");
                textType.setText("筆記");
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(FengCommonActivity.this)
                        .inflate(R.layout.item_common_child, parent, false);
            }

            TextView textContent = convertView.findViewById(R.id.textChildContent);
            String content = (String) getChild(groupPosition, childPosition);
            textContent.setText(content);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
