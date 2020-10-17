package ru.euphoria.doggy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.euphoria.doggy.adapter.LinksAdapter;
import ru.euphoria.doggy.api.model.Link;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;

public class LinksFragment extends BaseAttachmentsFragment<Link> {
    private LinksAdapter adapter;

    public static LinksFragment newInstance(int peer) {
        Bundle args = new Bundle();
        args.putInt("peer", peer);

        LinksFragment fragment = new LinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        Disposable subscribe = flowable.subscribe(links -> {
            adapter = new LinksAdapter(getActivity(), links);
            recycler.setAdapter(adapter);

            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                Link item = adapter.getItem(position);
                AndroidUtil.browse(getActivity(), item.url);
            });
        });
        disposable.add(subscribe);
        return root;
    }


    @Override
    public Flowable<List<Link>> getFlowable() {
        return AppDatabase.database().links()
                .byPeer(getPeerId())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }
}
