package ru.euphoria.doggy.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.AudiosFragment;
import ru.euphoria.doggy.DocumentsFragment;
import ru.euphoria.doggy.LinksFragment;
import ru.euphoria.doggy.PhotosFragment;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.VideosFragment;
import ru.euphoria.doggy.VoicesFragment;

public class AttachmentsPagerAdapter extends BaseFragmentPagerAdapter {
    private int peer;

    public AttachmentsPagerAdapter(FragmentManager fm, int peer) {
        super(fm);
        this.peer = peer;
    }

    @Override
    public String[] getTitles() {
        return AppContext.context.getResources().getStringArray(R.array.attachments);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return PhotosFragment.newInstance(peer);
            case 1:
                return VideosFragment.newInstance(peer);
            case 2:
                return AudiosFragment.newInstance(peer);
            case 3:
                return DocumentsFragment.newInstance(peer);
            case 4:
                return LinksFragment.newInstance(peer);
            case 5:
                return VoicesFragment.newInstance(peer);
        }
        return PhotosFragment.newInstance(peer);
    }
}
