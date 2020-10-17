package ru.euphoria.doggy;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.appbar.CollapsingToolbarLayout;

public class BaseSettingsFragment extends PreferenceFragmentCompat {
   public BaseSettingsFragment(String title) {
       Bundle args = new Bundle();
       args.putString("title", title);
       setArguments(args);
   }

    @Override
    public void onResume() {
        super.onResume();

        CollapsingToolbarLayout toolbar = getActivity().findViewById(R.id.toolbar_layout);
        toolbar.setTitle(getTitle());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    public String getTitle() {
        return getArguments().getString("title");
    }
}