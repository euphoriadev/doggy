package ru.euphoria.doggy.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Photo;

@Dao
public interface PhotosDao extends BaseDao<Photo> {
    @Query("SELECT COUNT(*) FROM photos")
    int count();

    @Query("SELECT * FROM photos")
    Flowable<List<Photo>> all();

    @Query("SELECT * FROM photos WHERE has_geo = 1")
    LiveData<List<Photo>> onlyGeo();

    @Query("SELECT * FROM photos WHERE has_geo = 1 AND (msg_id > 0 OR peer_id > 0)")
    LiveData<List<Photo>> onlyDialogs();

    @Query("SELECT * FROM photos WHERE owner_id = :id")
    Flowable<List<Photo>> byOwner(int id);

    @Query("SELECT * FROM photos WHERE peer_id = :id ORDER BY msg_date DESC")
    Flowable<List<Photo>> byPeer(int id);
}
