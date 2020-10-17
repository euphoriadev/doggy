package ru.euphoria.doggy.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.ChatsFragment;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Chat;

public class ChatsPagerAdapter extends BaseFragmentPagerAdapter {
    private WeakHashMap<Integer, Fragment> sources = new WeakHashMap<>();

    public ChatsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = sources.get(position);
        if (fragment == null) {
            fragment = ChatsFragment.newInstance(position);
            sources.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public String[] getTitles() {
        return AppContext.context.getResources().getStringArray(R.array.chats_tabs);
    }

    public void search(String q) {
        Set<Map.Entry<Integer, Fragment>> entries = sources.entrySet();
        for (Map.Entry<Integer, Fragment> entry : entries) {
            Fragment fragment = entry.getValue();
            if (fragment != null) {
                ChatsAdapter adapter = ((ChatsFragment) fragment).adapter();
                if (adapter != null) {
                    adapter.search(q);
                }
            }
        }
    }

    public void refresh(ArrayList<Chat> chats) {
        Set<Map.Entry<Integer, Fragment>> entries = sources.entrySet();
        for (Map.Entry<Integer, Fragment> entry : entries) {
            ((ChatsFragment) entry.getValue()).createAdapter(chats);
        }
    }

    public void refreshForDeleted(int[] existsChats) {
        Set<Map.Entry<Integer, Fragment>> entries = sources.entrySet();
        for (Map.Entry<Integer, Fragment> entry : entries) {
            ((ChatsFragment) entry.getValue()).setDeletedChats(existsChats);
        }
    }
}
