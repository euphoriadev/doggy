package ru.euphoria.doggy.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.BuildConfig;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.LongPollEvent;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.UserUtil;

import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_CHANGE_FLAGS;
import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_NEW_MESSAGE;
import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_READ_MESSAGE_IN;
import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_READ_MESSAGE_OUT;
import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_TYPING_TEXT;
import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_TYPING_VOICE;
import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_USER_OFFLINE;
import static ru.euphoria.doggy.api.model.LongPollEvent.EVENT_USER_ONLINE;

public class MonitorService extends Service {
    private static final String TAG = "MonitorService";
    public static final String ACTION_LONG_POLL = BuildConfig.APPLICATION_ID + ".action.LONG_POLL";

    private UpdatesThread updater;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate");

        updater = new UpdatesThread();
        updater.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy");

        if (updater != null) {
            updater.running = false;
        }
    }

    private static User cacheUser(int peer) {
        peer = Math.abs(peer);
        if (peer > VKApi.PEER_OFFSET) {
            peer -= VKApi.PEER_OFFSET;
        }
        User user =  UserUtil.getCachedUser(peer);
        if (user == null) {
            user = UserUtil.getUser(peer).blockingGet();
            if (user != null) {
                AppDatabase.database().users().insert(user);
            }
        }
        return user;
    }

    public static abstract class EventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("onReceive: " + intent);
            int code = intent.getIntExtra("code", -1);
            JSONArray array = null;
            try {
                 array = new JSONArray(intent.getStringExtra("array"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (array == null) return;

            LongPollEvent event = intent.getParcelableExtra("event");
            switch (code) {
                case EVENT_CHANGE_FLAGS:
                    onMessageChangeFlags(event,
                            array.optInt(1),
                            array.optInt(2)
                    );
                    break;

                case EVENT_NEW_MESSAGE:
                    Message msg = intent.getParcelableExtra("msg");
                    if (msg == null) {
                        msg = AppDatabase.database().messages().byId(array.optInt(1));
                    }
                    onNewMessage(event, msg);
                    break;

                case EVENT_READ_MESSAGE_IN:
                case EVENT_READ_MESSAGE_OUT:
                    onReadMessage(event,
                            array.optInt(1),
                            array.optInt(2)
                    );
                    break;

                case EVENT_USER_ONLINE:
                case EVENT_USER_OFFLINE:
                    onUserOnline(event,
                            array.optInt(1),
                            code == EVENT_USER_ONLINE);
                    break;

                case EVENT_TYPING_TEXT:
                case EVENT_TYPING_VOICE:
                    onTyping(event,
                            array.optInt(1), code == EVENT_TYPING_VOICE);
                    break;

            }
        }

        public abstract void onMessageChangeFlags(LongPollEvent event, int id, int mask);

        public abstract void onNewMessage(LongPollEvent event, Message msg);

        public abstract void onReadMessage(LongPollEvent event, int peer, int local);

        public abstract void onUserOnline(LongPollEvent event, int id, boolean online);

        public abstract void onTyping(LongPollEvent event, int user, boolean voice);
    }

    private class UpdatesThread extends Thread {
        public boolean running = true;

        private String key, server, ts;

        @Override
        public void run() {
            Log.w(TAG, "Monitor Service Running");
            while (running) {
                if (!AndroidUtil.hasConnection()) {
                    try {
                        Thread.sleep(1000);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    JSONObject response;
                    if (server == null) {
                        response = VKApi.messages().getLongPollServer()
                                .put("lp_version", 3)
                                .json().optJSONObject("response");

                        key = response.optString("key");
                        server = response.optString("server");
                        ts = response.optString("ts");
                    }

                    response = getUpdates();
                    System.out.println("updates: " + response);

                    if (response.has("failed")) {
                        server = null;
                        continue;
                    }

                    ts = response.optString("ts");
                    JSONArray updates = response.optJSONArray("updates");
                    handle(updates);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void handle(JSONArray response) throws Exception {
            if (response.length() == 0) return;

            for (int i = 0; i < response.length(); i++) {
                JSONArray array = response.optJSONArray(i);
                int code = array.optInt(0);
                int message_id;
                Message msg;
                Intent intent;

                LongPollEvent event = new LongPollEvent(
                        code, System.currentTimeMillis(), array.toString()
                );

                intent = new Intent(ACTION_LONG_POLL);
                intent.putExtra("code", code);
                intent.putExtra("event", event);
                intent.putExtra("array", array.toString());

                switch (code) {
                    // new message
                    case EVENT_NEW_MESSAGE:
                        message_id = array.optInt(1);
                        msg = VKApi.from(Message.class, VKApi.messages()
                                .getById()
                                .messageIds(message_id)
                                .json()).get(0);
                        event.msg = msg;
                        event.user = cacheUser(msg.from_id);

                        AppDatabase.database().messages().insert(msg);

                        intent.putExtra("msg", msg);
                        break;

                    case EVENT_READ_MESSAGE_IN:
                    case EVENT_READ_MESSAGE_OUT:

                    case EVENT_USER_ONLINE:
                    case EVENT_USER_OFFLINE:

                    case EVENT_TYPING_TEXT:
                    case EVENT_TYPING_VOICE:
                        event.user = cacheUser(array.optInt(1));
                        break;
                }
                AppDatabase.database().events().insert(event);
                LocalBroadcastManager.getInstance(AppContext.context).sendBroadcast(intent);
            }
        }

        private JSONObject getUpdates() throws Exception {
            HttpUrl url = HttpUrl.get("https://" + server).newBuilder()
                    .addQueryParameter("act", "a_check")
                    .addQueryParameter("key", key)
                    .addQueryParameter("ts", ts)
                    .addQueryParameter("wait", "25")
                    .addQueryParameter("mode", "64")
                    .addQueryParameter("version", "3")
                    .build();

            return new JSONObject(AndroidUtil.requestSync(url.toString()));
        }
    }
}
