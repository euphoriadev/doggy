package ru.euphoria.doggy;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.euphoria.doggy.adapter.MonitorEventsAdapter;
import ru.euphoria.doggy.api.model.LongPollEvent;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.service.MonitorService;

public class MonitorFragment extends Fragment {
    private MonitorService.EventReceiver receiver;
    private MonitorEventsAdapter adapter;
    private RecyclerView recycler;

    public static MonitorFragment newInstance() {
        Bundle args = new Bundle();

        MonitorFragment fragment = new MonitorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_monitor, container, false);
        recycler =  root.findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        receiver = new MonitorService.EventReceiver() {
            @Override
            public void onMessageChangeFlags(LongPollEvent event, int id, int mask) {
                if (Message.isDeleted(mask)) {
                    Message msg = AppDatabase.database().messages().byId(id);
                    event.text = "Удалил сообщение\"" + msg.text + "\"";
                }
                adapter.insert(event);
            }

            @Override
            public void onNewMessage(LongPollEvent event, Message msg) {
                event.text = msg.text;

                adapter.insert(event);
            }

            @Override
            public void onReadMessage(LongPollEvent event, int peer, int local) {
                event.text = "Прочитал сообщения";
                adapter.insert(event);
            }

            @Override
            public void onUserOnline(LongPollEvent event, int id, boolean online) {
                if (online) {
                    event.text = "Появился в сети";
                } else {
                    event.text = "Вышел из сети";
                }
                adapter.insert(event);
            }

            @Override
            public void onTyping(LongPollEvent event, int user, boolean voice) {
                if (voice) {
                    event.text = "Записывает аудиосообщение...";
                } else {
                    event.text = "Печатает...";
                }
                adapter.insert(event);
            }
        };
        adapter = new MonitorEventsAdapter(getContext(), new ArrayList<>());
        recycler.setAdapter(adapter);

        List<LongPollEvent> events = AppDatabase.database().events().all();
        for (LongPollEvent event : events) {
            Intent intent = new Intent(MonitorService.ACTION_LONG_POLL);
            intent.putExtra("code", event.code);
            intent.putExtra("array", event.updates);
            intent.putExtra("event", event);

            receiver.onReceive(getContext(), intent);
        }

        LocalBroadcastManager.getInstance(AppContext.context).registerReceiver(receiver,
                new IntentFilter(MonitorService.ACTION_LONG_POLL));
        return root;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(AppContext.context).unregisterReceiver(receiver);
    }
}
