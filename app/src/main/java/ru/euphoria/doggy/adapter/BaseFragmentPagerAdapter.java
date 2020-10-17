package ru.euphoria.doggy.adapter;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract class BaseFragmentPagerAdapter extends FragmentPagerAdapter {
    public BaseFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public abstract String[] getTitles();

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return getTitles().length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return getTitles()[position];
    }
}
