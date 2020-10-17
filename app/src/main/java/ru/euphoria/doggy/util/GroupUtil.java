package ru.euphoria.doggy.util;

import android.content.Context;
import android.util.SparseArray;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.db.AppDatabase;

public class GroupUtil {
    private static SparseArray<Community> cache = new SparseArray<>();

    public static void appendToCache(List<Community> groups) {
        for (Community group : groups) {
            cache.append(group.id, group);
        }
    }

    public static Community getCachedGroup(int id) {
        Community group = cache.get(id);
        if (group == null) {
            group = AppDatabase.database().groups().byId(id);
            if (group != null) {
                cache.append(id, group);
            }
        }
        return group;
    }


    @SuppressWarnings("unchecked")
    public static Flowable<ArrayList<User>> getMembers(Context context, int group, String fields) {
        String script = VKUtil.script(context, VKApi.GROUPS_GET_MEMBERS, group, fields);

        return VKApi.execute(script).async(User.class);
    }

    public static Single<ArrayList<Community>> getMyGroups() {
        return Single.fromCallable(() -> {
            JSONObject json = VKApi.groups().get()
                    .count(1000)
                    .fields("members_count")
                    .extended(true)
                    .json();
            return VKApi.from(Community.class, json);
        }).subscribeOn(Schedulers.io());
    }

    public static Single<ArrayList<Community>> getGroups(int... ids) {
        return Single.fromCallable(() -> {
            JSONObject json = VKApi.groups().getById()
                    .groupIds(ids)
                    .fields("members_count")
                    .json();
            return VKApi.from(Community.class, json);
        }).subscribeOn(Schedulers.io());
    }
}
