package ru.euphoria.doggy.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.User;

@Dao
public interface UsersDao extends BaseDao<User> {
    @Query("SELECT COUNT(*) FROM users")
    int count();

    @Query("DELETE FROM users WHERE id = :id")
    void delete(int id);

    @Query("SELECT * FROM users")
    Flowable<List<User>> all();

    @Query("SELECT * FROM users WHERE is_friend = 1 ORDER BY insert_time")
    LiveData<List<User>> friends();

    @Query("SELECT * FROM users WHERE id = :id")
    Flowable<User> byId(int id);

    @Query("SELECT * FROM users WHERE id = :id")
    User byIdSync(int id);
}
