package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Video;

@Dao
public interface VideosDao extends BaseDao<Video> {
    @Query("SELECT COUNT(*) FROM videos")
    int count();

    @Query("SELECT * FROM videos ORDER BY id DESC")
    Flowable<List<Video>> all();

    @Query("SELECT * FROM videos WHERE peer_id = :id ORDER BY msg_date DESC")
    Flowable<List<Video>> byPeer(int id);
}
