package ru.euphoria.doggy.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.Response;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;

/**
 * Created by admin on 20.04.18.
 */

public class UserUtil {
    private static SparseArray<User> cache = new SparseArray<>();

    public static void appendToCache(List<User> users) {
        for (User user : users) {
            cache.append(user.id, user);
        }
    }

    private static <T> Single<T> from(Callable<T> callable) {
        return Single.fromCallable(callable).subscribeOn(Schedulers.io());
    }

    private static void addConnection(ArrayList<Pair<String, String>> connections, String type, String value) {
        if (!TextUtils.isEmpty(value)) {
            connections.add(Pair.create(type, value));
        }
    }

    public static String formatNumber(String number) {
        String country = Locale.getDefault().getCountry();
        return PhoneNumberUtils.formatNumberToE164(number, country);
    }

    public static ArrayList<Pair<String, String>> getConnections(User user) {
        ArrayList<Pair<String, String>> connections = new ArrayList<>(4);
        addConnection(connections, "Skype", user.skype);
        addConnection(connections, "Facebook", user.facebook);
        addConnection(connections, "Twitter", user.twitter);
        addConnection(connections, "Instagram", user.instagram);
        return connections;
    }


    public static boolean validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        try {
            Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(phone, Locale.getDefault().getCountry());
            return PhoneNumberUtil.getInstance().isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static boolean isPossibleNumber(String phone) {
        return PhoneNumberUtil.getInstance().isPossibleNumber(phone, Locale.getDefault().getCountry());
    }

    public static int[] toIds(ArrayList<User> users) {
        int[] ids = new int[users.size()];
        for (int i = 0; i < users.size(); i++) {
            ids[i] = users.get(i).id;
        }
        return ids;
    }

    public static Single<String> getRegDate(int id) {
        return from(() -> {
            Request.Builder builder = new Request.Builder();
            builder.url("https://vk.com/foaf.php?id=" + id);

            Response response = AppContext.httpClient.newCall(builder.build()).execute();
            return AndroidUtil.readStream(response.body()
                    .byteStream(), "WINDOWS-1251");
        });
    }

    public static ArrayList<User> getCachedUsers(int... ids) {
        ArrayList<User> users = new ArrayList<>();
        for (int id : ids) {
            User user = getCachedUser(id);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    public static User me() {
        return getCachedUser(SettingsStore.getUserId());
    }

    public static synchronized User getCachedUser(int id) {
        User user = cache.get(id);
        if (user == null) {
            user = AppDatabase.database().users().byIdSync(id);
            if (user != null) {
                cache.append(id, user);
            }
        }
        return user;
    }

    public static Single<User> getUser(int id) {
        return getUsers(id).map(users -> users.get(0));
    }

    public static Single<User> getUser() {
        return getUsers().map(users -> users.get(0));
    }

    public static Single<ArrayList<User>> getUsers(int... ids) {
        return AppContext.users.get(ArrayUtil.join(ids, ','), User.DEFAULT_FIELDS)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Single<ArrayList<User>> getUsers() {
        return from(() -> {
            JSONObject json = VKApi.users().get()
                    .fields(User.DEFAULT_FIELDS)
                    .json();
            return VKApi.from(User.class, json);
        }).subscribeOn(Schedulers.io());
    }

    public static int getMonth(User user) throws Exception {
        for (int i = 1; i <= 12; i++) {
            JSONObject json = VKApi.users().search()
                    .q(user.toString())
                    .birthMonth(i)
                    .json();

            json = json.optJSONObject("response");
            if (json != null && json.optInt("count") > 0) {
                ArrayList<User> users = VKApi.from(User.class, json);
                for (User item : users) {
                    if (item.id == user.id) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    public static int getAge(Context context, User user) throws Exception {
        String script = String.format(AndroidUtil.loadAssestsFile(context,
                VKApi.FRIENDS_GET_AGE_SCRIPT),
                user.toString());

        JSONObject json = VKApi.execute(script).json();
        JSONObject response = json.optJSONObject("response");
        return response != null ? response.optInt("age") : 0;
    }

    public static Single<List<User>> getFriends(Context context) {
        return getFriends(context, "hints");
    }

    public static Single<List<User>> getFriends(Context context, String order) {
        return getFriends(context, SettingsStore.getUserId(), order);
    }

    @SuppressLint("CheckResult")
    public static Single<List<User>> getFriends(Context context, int userId, String order) {
        return Single.create((SingleOnSubscribe<List<User>>) emitter -> {
            ArrayList<User> output = new ArrayList<>();

            VKUtil.getAllFriends(userId, 0, order)
                    .flatMapIterable(users -> users)
                    .flatMap(user -> {
                        user.is_friend = true;
                        return Flowable.just(user);
                    })
                    .doFinally(() -> emitter.onSuccess(output))
                    .subscribe(output::add, emitter::onError);
        }).subscribeOn(Schedulers.io());
    }

    public static SparseArray<int[]> getFriendsIds(Context context, int... ids) throws Exception {
        String script = String.format(AndroidUtil.loadAssestsFile(context,
                VKApi.FRIENDS_GET_IDS), "[" + ArrayUtil.join(ids, ',') + "]");

        JSONArray response = VKApi.execute(script)
                .json()
                .optJSONArray("response");

        SparseArray<int[]> array = new SparseArray<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject item = response.optJSONObject(i);
            int owner = item.optInt("owner");
            int count = item.optInt("count");
            if (count > 0) {
                int[] friends = VKApi.parseArray(item.optJSONArray("friends"));
                array.append(owner, friends);
            }
        }
        return array;
    }

    public static Flowable<ArrayList<Pair<Integer, String>>> getFriendLists(Context context, int user, int friend) {
        return Flowable.fromCallable(() -> {
            String script = String.format(AndroidUtil.loadAssestsFile(context,
                    VKApi.FRIENDS_GET_LISTS_SCRIPTS),
                    user, friend);
            JSONObject json = VKApi.execute(script).json();
            JSONObject response = json.optJSONObject("response");

            int[] lists = VKApi.parseArray(response.optJSONArray("lists"));
            if (lists != null) {
                JSONArray items = response.optJSONArray("items");
                ArrayList<Pair<Integer, String>> output = new ArrayList<>();
                for (int id : lists) {
                    output.add(new Pair<>(id, nameForLists(id, items)));
                }
                return output;
            }
            return new ArrayList<>();
        });
    }

    private static String nameForLists(int id, JSONArray items) {
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item.optInt("id") == id) {
                return item.optString("name");
            }
        }
        return null;
    }
}
