package ru.euphoria.doggy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import ru.euphoria.doggy.adapter.FragmentPagerAdapter;
import ru.euphoria.doggy.adapter.MonitorFragmentPagerAdapter;
import ru.euphoria.doggy.service.MonitorService;

public class MonitorActivity extends BaseActivity {
    public static void start(Context context) {
        Intent starter = new Intent(context, MonitorActivity.class);
        context.startActivity(starter);
    }

    private ViewPager pager;
    private TabLayout tabs;
    private FragmentPagerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        getSupportActionBar().setTitle(R.string.item_monitor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new MonitorFragmentPagerAdapter(getSupportFragmentManager());
        ViewPager pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);

        startService(new Intent(this, MonitorService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, MonitorService.class));
    }
}
