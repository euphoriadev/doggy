package ru.euphoria.doggy;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ru.euphoria.doggy.adapter.UsersAdapter;

/**
 * Created by admin on 01.04.18.
 */

public class FriendsActivity extends BaseActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        getSupportActionBar().setTitle(R.string.friends_choice);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean dogsClean = getIntent()
                .getBooleanExtra("dogs_clean", false);
        if (dogsClean) {
            getSupportActionBar().setTitle(R.string.friends_dogs_clean);
        }

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.fragment_friends, FriendsFragment.newInstance(dogsClean))
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_friends, menu);

        MenuItem itemSort = menu.findItem(R.id.item_sort);
        MenuItem itemSearch = menu.findItem(R.id.item_search);

        searchView = (SearchView) itemSearch.getActionView();
        searchView.setQueryHint("Search People");
        searchView.setOnSearchClickListener(v -> {
            itemSort.setVisible(false);
            v.requestFocus();
        });
        searchView.setOnCloseListener(() -> {
            itemSort.setVisible(true);
            return true;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query == null) {
                    return false;
                }

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_friends);
                if (fragment != null) {
                    UsersAdapter adapter = ((FriendsFragment) fragment).adapter();
                    if (adapter != null) {
                        adapter.search(query);
                    }
                }
                return true;
            }
        });
        return true;
    }

}
