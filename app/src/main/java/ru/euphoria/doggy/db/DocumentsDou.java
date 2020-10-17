package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Document;

@Dao
public interface DocumentsDou extends BaseDao<Document> {
    @Query("SELECT COUNT(*) FROM docs")
    int count();

    @Query("SELECT * FROM docs WHERE peer_id = :id ORDER BY date DESC")
    Flowable<List<Document>> byPeer(int id);
}
