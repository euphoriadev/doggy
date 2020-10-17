package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Community;

@Dao
public interface GroupsDao extends BaseDao<Community> {
    @Query("SELECT COUNT(*) FROM groups")
    int count();

    @Query("SELECT * FROM groups WHERE id = :id")
    Community byId(int id);

    @Query("SELECT * FROM groups")
    Flowable<List<Community>> all();
}
