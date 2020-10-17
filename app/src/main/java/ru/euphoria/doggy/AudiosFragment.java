package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.euphoria.doggy.adapter.AudiosAdapter;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.service.AudioPlayerService;

@SuppressLint("CheckResult")
public class AudiosFragment extends BaseAttachmentsFragment<Audio> {
    private AudiosAdapter adapter;

    public static AudiosFragment newInstance(int peer) {
        Bundle args = new Bundle();
        args.putInt("peer", peer);

        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        Disposable subscribe = flowable.subscribe(audio -> {
            adapter = new AudiosAdapter(getActivity(), audio);
            recycler.setAdapter(adapter);
            recycler.setItemAnimator(null);

            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);

                AudioPlayerService.play(getActivity(),
                        (ArrayList<? extends Audio>) adapter.getValues(), position);
            });
        });
        disposable.add(subscribe);
        return root;
    }

    @Override
    public Flowable<List<Audio>> getFlowable() {
        return AppDatabase.database().audios()
                        .byPeer(getPeerId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .cache();
    }
}
