package ru.euphoria.doggy.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.MonitorFragment;
import ru.euphoria.doggy.R;

public class MonitorFragmentPagerAdapter extends BaseFragmentPagerAdapter {
    public MonitorFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return MonitorFragment.newInstance();
    }

    @Override
    public String[] getTitles() {
        return AppContext.context.getResources().getStringArray(R.array.monitor_tabs);
    }
}
