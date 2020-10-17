package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.common.Stopwatch;
import ru.euphoria.doggy.db.AppDatabase;


@SuppressLint("CheckResult")
public class TestActivity extends BaseActivity {
    public static void start(Context context, int peer_id) {
        Intent starter = new Intent(context, TestActivity.class);
        starter.putExtra("peer", peer_id);
        context.startActivity(starter);
    }

    @BindView(R.id.text) TextView info;

    int peer_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        info.setText("SQLite Version: " + AppContext.getSQLiteVersion());
        peer_id = getIntent().getIntExtra("peer", 0);
    }

    public void run(View v) {
        execute(() -> {
            for (int i = 0; i < 5; i++) {
                Debug.startMethodTracing();
                runTest(peer_id);
                Debug.stopMethodTracing();
            }
        });
    }

    private void runTest(int peer) {
        runOnUiThread(() -> info.append("\n\nStarting test for " + peer));

        Stopwatch stopwatch = Stopwatch.createStarted();
        try (Cursor cursor = getCachedMessagesCursor(peer)) {
            ArrayList<Message> messages = new ArrayList<>(cursor.getCount());

            int fromId = cursor.getColumnIndex("id");
            int fromIndex = cursor.getColumnIndex("from_id");
            int peerIndex = cursor.getColumnIndex("peer_id");
            int bodyIndex = cursor.getColumnIndex("text");
            int dateIndex = cursor.getColumnIndex("date");
            int emojiIndex = cursor.getColumnIndex("emoji");

            while (cursor.moveToNext()) {
                Message msg = new Message();
                msg.id = cursor.getInt(fromId);
                msg.from_id = cursor.getInt(fromIndex);
                msg.peer_id = cursor.getInt(peerIndex);
                msg.date = cursor.getInt(dateIndex);
                msg.text = cursor.getString(bodyIndex);
                msg.emoji = cursor.getInt(emojiIndex) == 1;

                messages.add(msg);

                int size = messages.size();
                if (size % 50_000 == 0) {
                    System.out.println("Current messages size: " + size);
                }
            }

            String elapsed = stopwatch.toString();
            runOnUiThread(() -> info.append("\nEnd test." + messages.size() + " rows. Time: " + elapsed));
        }
    }

    private Cursor getCachedMessagesCursor(int peer) {
        return AppDatabase.database().messages().cursorByPeer(peer);
    }
}
