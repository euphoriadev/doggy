package ru.euphoria.doggy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import ru.euphoria.doggy.common.DebugLog;

public abstract class BaseAttachmentsFragment<T> extends Fragment {
    private final String TAG = getClass().getSimpleName();

    public CompositeDisposable disposable = new CompositeDisposable();
    public RecyclerView recycler;
    public Flowable<List<T>> flowable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.w(TAG, "onCreate");
        setHasOptionsMenu(false);

        flowable = getFlowable();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        DebugLog.w(TAG, "onCreateView");

        View root = inflater.inflate(layout(), container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recycler = root.findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugLog.w(TAG, "onDestroy");
        disposable.dispose();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DebugLog.w(TAG, "onDestroyView");
    }

    public int layout() {
        return R.layout.recycler;
    }

    public abstract Flowable<List<T>> getFlowable();

    public int getPeerId() {
        return getArguments().getInt("peer", -1);
    }
}
