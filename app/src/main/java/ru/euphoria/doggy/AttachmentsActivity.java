package ru.euphoria.doggy;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

import ru.euphoria.doggy.adapter.AttachmentsPagerAdapter;

public class AttachmentsActivity extends BaseActivity {
    private TabLayout tabs;
    private PagerAdapter adapter;
    private int peer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachments);
        getSupportActionBar().setTitle(R.string.attachments_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        peer = getIntent().getIntExtra("peer", 0);
        adapter = new AttachmentsPagerAdapter(getSupportFragmentManager(), peer);

        ViewPager pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);
    }

    private void makeBadge(int position, int number) {
        BadgeDrawable badge = tabs.getTabAt(position).getOrCreateBadge();
        badge.setVisible(true);
        badge.setNumber(number);
    }

    public TabLayout tabs() {
        return tabs;
    }
}
