package ru.euphoria.doggy.util;

import android.util.SparseArray;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.api.ConversationResponse;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.Chat;
import ru.euphoria.doggy.api.model.Conversation;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.db.AppDatabase;

import static ru.euphoria.doggy.api.VKApi.MESSAGES_GET_DIALOGS_ALL;

/**
 * Created by admin on 20.04.18.
 */

public class MessageUtil {
    private static SparseArray<Message> cache = new SparseArray<>();

    public static synchronized Message getCachedMessage(int id) {
        Message msg = cache.get(id);
        if (msg == null) {
            msg = AppDatabase.database().messages().byId(id);
            if (msg != null) {
                cache.append(id, msg);
            }
        }
        return msg;
    }

    public static int count(JSONObject json) {
        return VKApi.optCount(json);
    }

    public static String formatScript(String script, int peer, int offset) {
        return String.format(script, peer, offset);
    }

    public static String script() throws IOException {
        return script(VKApi.MESSAGES_GET_HISTORY_SCRIPT);
    }

    public static String script(String name) throws IOException {
        return AndroidUtil.loadAssestsFile(AppContext.context, name);
    }

    public static Flowable<Integer> getHistoryCount(int peer) {
        return Flowable.fromCallable(() -> {
            JSONObject json = VKApi.messages().getHistory()
                    .peerId(peer)
                    .count(0)
                    .json();
            return count(json);
        });
    }

    public static Flowable<Message> getChat(int id) {
        return Flowable.fromCallable(() -> {
            JSONObject json = VKApi.messages()
                    .getChat()
                    .chatId(id)
                    .json();

            JSONObject source = json.optJSONObject("response");
            Message message = new Message(source);
            message.peer_id = id;
            message.chat_id = id;
            return message;
        });
    }

    public static Single<Integer> sendMessage(int peer, String message) {
        return Single.fromCallable(() -> VKApi.messages().send()
                .peerId(peer)
                .message(message)
                .json().optInt("response")).subscribeOn(Schedulers.io());
    }

    public static Single<ConversationResponse> getConversations(int offset) {
        return getConversations(offset, 50);
    }

    public static Single<ConversationResponse> getConversations(int offset, int count) {
        return getConversations(offset, count, true);
    }

    public static Single<ArrayList<Chat>> getChats(int... ids) {
        return Single.fromCallable(() -> {
            JSONObject json = VKApi.messages().getChat().chatIds(ids).json();
            return VKApi.from(Chat.class, json);
        }).subscribeOn(Schedulers.io());
    }

    public static Single<ArrayList<Conversation>> getConversationsAll(boolean onlyChats) {
        return Single.fromCallable(() -> {
            String script = script(MESSAGES_GET_DIALOGS_ALL);
            JSONObject json = VKApi.execute(script).json();
            ArrayList<Conversation> items = VKApi.from(Conversation.class, json);

            if (onlyChats) {
                ArrayUtil.filter(items, conversation -> "chat".equals(conversation.peer.type));
            }

            return items;
        }).subscribeOn(Schedulers.io());
    }

    public static Single<ConversationResponse> getConversations(int offset, int count, boolean extended) {
        return Single.fromCallable(() -> {
            JSONObject json = VKApi.messages()
                    .getConversations()
                    .offset(offset)
                    .count(count)
                    .fields(User.DEFAULT_FIELDS)
                    .extended(extended)
                    .json();

            return new ConversationResponse(json);
        }).subscribeOn(Schedulers.io());
    }

    public static Single<Boolean> removeChatUser(int chat, int user) {
        return Single.fromCallable(() -> {
            JSONObject json = VKApi.messages().removeChatUser()
                    .chatId(chat)
                    .userId(user)
                    .fields(User.DEFAULT_FIELDS)
                    .json();
            return json.has("response");
        }).subscribeOn(Schedulers.io());
    }

    public static Single<ArrayList<User>> getMembers(int peer) {
        return Single.fromCallable(() -> {
            JSONObject json = VKApi.messages().getChatUsers()
                    .chatId(peer - VKApi.PEER_OFFSET)
                    .fields(User.DEFAULT_FIELDS)
                    .json();

            return VKApi.from(User.class, json);
        }).subscribeOn(Schedulers.io());
    }
}
