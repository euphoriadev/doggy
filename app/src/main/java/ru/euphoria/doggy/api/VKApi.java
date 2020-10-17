package ru.euphoria.doggy.api;

import android.text.TextUtils;

import com.yandex.metrica.YandexMetrica;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import okhttp3.Request;
import okhttp3.Response;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.SettingsFragment;
import ru.euphoria.doggy.api.method.ParamSetter;
import ru.euphoria.doggy.api.method.SearchParamSetter;
import ru.euphoria.doggy.api.model.Chat;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.VKUtil;

/**
 * Created by admin on 24.03.18.
 */

public class VKApi {
    public static final String FRIENDS_GET_SCRIPT = "friends_get.txt";
    public static final String FRIENDS_GET_LISTS_SCRIPTS = "friend_get_lists.txt";
    public static final String FRIENDS_GET_IDS = "friends_get_ids.txt";
    public static final String FRIENDS_GET_AGE_SCRIPT = "friends_get_birthday_year.txt";
    public static final String FRIENDS_GET_MONTH_SCRIPT = "friends_get_birthday_month..txt";
    public static final String MESSAGES_GET_HISTORY_SCRIPT = "messages_get_history.txt";
    public static final String MESSAGES_GET_DIALOGS_ALL = "dialogs_get_all";
    public static final String GROUPS_GET_MEMBERS = "groups_get_members.txt";

    public static final String BASE_URL = "https://api.vk.com/method/";
    public static final int PEER_OFFSET = 2_000_000_000;
    public static final double API_VERSION = 5.89;

    public static final String API_PROXY = "vk-api-proxy.xtrafrancyz.net";
    public static final String OAUTH_PROXY = "vk-oauth-proxy.xtrafrancyz.net";
    public static final String API_DOMAIN = "api.vk.com";
    public static final String OAUTH_DOMAIN = "oauth.vk.com";

    public static String apiDomain;
    public static String oauthDomain;

    public static void initBaseUrls() {
        apiDomain = SettingsStore.getString(SettingsFragment.KEY_API_DOMAIN, API_DOMAIN);
        oauthDomain = SettingsStore.getString(SettingsFragment.KEY_OAUTH_DOMAIN, OAUTH_DOMAIN);
    }

    public static class UserMethods {
        private UserMethods() {

        }

        public SearchParamSetter search() {
            return new SearchParamSetter("users.search");
        }

        public ParamSetter get() {
            return new ParamSetter("users.get");
        }

        public ParamSetter report() {
            return new ParamSetter("users.report");
        }
    }

    public static class MessagesMethods {
        private MessagesMethods() {

        }

        public ParamSetter getById() {
            return new ParamSetter("messages.getById");
        }

        public ParamSetter getLongPollServer() {
            return new ParamSetter("messages.getLongPollServer");
        }

        public ParamSetter deleteConversation() {
            return new ParamSetter("messages.deleteConversation");
        }

        public ParamSetter removeChatUser() {
            return new ParamSetter("messages.removeChatUser");
        }

        public ParamSetter getChatUsers() {
            return new ParamSetter("messages.getChatUsers");
        }

        public ParamSetter send() {
            return new ParamSetter("messages.send");
        }

        public ParamSetter getHistory() {
            return new ParamSetter("messages.getHistory");
        }

        public ParamSetter getDialogs() {
            return new ParamSetter("messages.getDialogs");
        }

        public ParamSetter<Chat> getChat() {
            return new ParamSetter<>("messages.getChat");
        }

        public ParamSetter getConversations() {
            return new ParamSetter("messages.getConversations");
        }

        public ParamSetter restore(int id) {
            return new ParamSetter("messages.restore").put("message_id", String.valueOf(id));
        }
    }

    public static class AudiosMethods {
        private AudiosMethods() {

        }

        public ParamSetter get() {
            return new ParamSetter("audio.get");
        }

        public ParamSetter getLyrics(int id) {
            return new ParamSetter("audio.getLyrics")
                    .put("lyrics_id", id);
        }

        public ParamSetter edit() {
            return new ParamSetter("audio.edit");
        }

        public ParamSetter delete() {
            return new ParamSetter("audio.delete");
        }
    }


    public static class AccountMethods {
        private AccountMethods() {

        }

        public ParamSetter ban() {
            return new ParamSetter("account.ban");
        }

        public ParamSetter setOnline() {
            return new ParamSetter("account.setOnline");
        }
    }

    public static class GroupsMethods {
        private GroupsMethods() {

        }

        public ParamSetter get() {
            return new ParamSetter("groups.get");
        }

        public ParamSetter getById() {
            return new ParamSetter("groups.getById");
        }

        public ParamSetter isMember() {
            return new ParamSetter("groups.isMember");
        }

        public ParamSetter join() {
            return new ParamSetter("groups.join");
        }

        public ParamSetter leave() {
            return new ParamSetter("groups.leave");
        }
    }

    public static class PhotosMethods {
        private PhotosMethods() {

        }

        public ParamSetter<Photo> search() {
            return new ParamSetter<>("photos.search");
        }
    }


    public static class FriendMethods {
        private FriendMethods() {

        }

        public ParamSetter getRequests() {
            return new ParamSetter("friends.getRequests").put("out", 1);
        }

        public ParamSetter getLists() {
            return new ParamSetter("friends.getLists")
                    .put("return_system", 1);
        }

        public ParamSetter delete() {
            return new ParamSetter("friends.delete");
        }
    }

    public static class AppsMethods {
        private AppsMethods() {

        }

        public ParamSetter get() {
            return new ParamSetter("apps.get");
        }
    }

    public static class WallsMethods {
        private WallsMethods() {

        }

        public ParamSetter post() {
            return new ParamSetter("wall.post");
        }

        public ParamSetter delete() {
            return new ParamSetter("friends.delete");
        }
    }

    public static class AuthMethods {
        public ParamSetter refreshToken(String receipt) {
            return new ParamSetter("auth.refreshToken")
                    .put("receipt", receipt.replaceAll("[\\w%\\-]+:", ":"));
        }
    }

    public static FriendMethods friends() {
        return new FriendMethods();
    }

    public static AudiosMethods audios() {
        return new AudiosMethods();
    }

    public static UserMethods users() {
        return new UserMethods();
    }

    public static MessagesMethods messages() {
        return new MessagesMethods();
    }

    public static AccountMethods account() {
        return new AccountMethods();
    }

    public static WallsMethods walls() {
        return new WallsMethods();
    }

    public static GroupsMethods groups() {
        return new GroupsMethods();
    }

    public static PhotosMethods photos() {
        return new PhotosMethods();
    }

    public static AuthMethods auth() {
        return new AuthMethods();
    }

    public static AppsMethods apps() {
        return new AppsMethods();
    }

    public static ParamSetter statsTrackVisitor() {
        return new ParamSetter("stats.trackVisitor");
    }

    public static ParamSetter execute(String code) {
        return new ParamSetter("execute")
                .put("code", code);
    }

    public static ParamSetter createParamSetter(String methodName) {
        return new ParamSetter(methodName);
    }

    public static String body(Request request) throws IOException {
        Response response = AppContext.httpClient.newCall(request).execute();
        String body = response.body().string();

        DebugLog.i("Network.Response", body);
        return body;
    }

    public static JSONObject json(Request request) throws Exception {
        try {
            String body = body(request);
            JSONObject json = new JSONObject(body);
            VKApi.checkError(json, request.url().toString());
            return json;
        } catch (VKException ex) {
            ex.printStackTrace();
            if (ex.code == ErrorCodes.TOO_MANY_REQUESTS) {
                Thread.sleep(1000);
                return json(request);
            }
            throw ex;
        }
    }

    public static void checkError(JSONObject json, String url) throws VKException {
        VKUtil.checkError(json, url);
    }

    public static <E> ArrayList<E> from(Class<E> eClass, JSONObject json) {
        return from(eClass, optJsonArray(json));
    }

    public static <E> ArrayList<E> from(Class<E> eClass, JSONArray array) {
        return from(eClass, array, null);
    }

    public static <E> E fromSingle(Class<E> eClass, JSONObject json) {
        try {
            Constructor<E> constructor = eClass.getConstructor(JSONObject.class);
            return constructor.newInstance(json);
        } catch (Exception e) {
            e.printStackTrace();
            YandexMetrica.reportError("Ошибка парсинга json", e);
        }
        return null;
    }

    public static <E> ArrayList<E> from(Class<E> eClass, JSONArray array, String name) {
        ArrayList<E> list = new ArrayList<>(array == null ? 0 : array.length());
        if (array == null) {
            return list;
        }

        try {
            Constructor<E> constructor = eClass.getConstructor(JSONObject.class);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (!TextUtils.isEmpty(name) && item.has(name)) {
                    item = item.optJSONObject(name);
                }
                list.add(constructor.newInstance(item));
            }

        } catch (Exception e) {
            e.printStackTrace();
            YandexMetrica.reportError("Ошибка парсинга json", e);
        }
        return list;
    }

    public static JSONObject optJsonObject(JSONObject json) {
        return json.optJSONObject("response");
    }

    public static int optCount(JSONObject json) {
        return json.optJSONObject("response").optInt("count");
    }

    public static JSONArray optJsonArray(JSONObject json, String name) {
        return VKUtil.optJsonArray(json, name);
    }

    public static JSONArray optJsonArray(JSONObject json) {
        return optJsonArray(json, "items");
    }

    public static int[] parseArray(JSONArray array) {
        return VKUtil.parseArray(array);
    }
}
