package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Link;

@Dao
public interface LinksDao extends BaseDao<Link> {
    @Query("SELECT COUNT(*) FROM links")
    int count();

    @Query("SELECT * FROM links WHERE peer_id = :id")
    Flowable<List<Link>> byPeer(int id);
}
