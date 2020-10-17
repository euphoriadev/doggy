package ru.euphoria.doggy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yandex.metrica.YandexMetrica;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.adapter.ChatsAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.Chat;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;

public class ChatsFragment extends Fragment {
    private ChatsAdapter adapter;
    private RecyclerView recycler;
    private int position;

    public static ChatsFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt("position", position);

        ChatsFragment fragment = new ChatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.recycler, container, false);

        LinearLayoutManager layoutManager = new WrapLinearLayoutManager(getActivity());

        recycler = root.findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);

        position = getArguments().getInt("position");
        return root;
    }

    public void setDeletedChats(int... ids) {
        Arrays.sort(ids);

        adapter.deletedChats = ids;
        if (position == 1) {
            filter(ids);
            recycler.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    public ChatsAdapter adapter() {
        return adapter;
    }

    public void createAdapter(ArrayList<Chat> chats) {
        adapter = new ChatsAdapter(getActivity(), chats);
        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            Chat chat = adapter.getItem(position);
            createOptionsDialog(chat);
        });

        if (position != 1) {
            recycler.setAdapter(adapter);
        }
    }

    private void filter(int[] ids) {
        ArrayList<Chat> copy = new ArrayList<>(adapter.getValues());
        ArrayUtil.filter(copy, chat -> Arrays.binarySearch(ids, chat.id) >= 0);
        adapter.setValues(copy);
    }

    private void createOptionsDialog(Chat chat) {
        String[] items = getResources().getStringArray(R.array.chat_options);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(chat.title);
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case 0:
                    AndroidUtil.browse(getActivity(), chat);
                    break;
                case 1:
                    joinChat(chat.id);
                    break;
            }
        });
        builder.show();
    }

    private void joinChat(int id) {
        if (adapter.deletedChats != null && Arrays.binarySearch(adapter.deletedChats, id) < 0) {
            AndroidUtil.toast(getActivity(), "You has already in chat");
            return;
        }

        Single.fromCallable(() -> {
            JSONObject json = VKApi.messages().send().chatId(id).message(".").json();
            return json.has("response");
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), R.string.messages_chat_restore_success, Toast.LENGTH_SHORT)
                                .show();
                        YandexMetrica.reportEvent("Восстановление чата");

                    }
                }, AndroidUtil.handleError(getActivity()));
    }
}
