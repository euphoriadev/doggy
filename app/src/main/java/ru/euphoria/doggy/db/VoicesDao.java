package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.AudioMessage;

@Dao
public interface VoicesDao extends BaseDao<AudioMessage> {
    @Query("SELECT COUNT(*) FROM voices")
    int count();

    @Query("SELECT * FROM voices WHERE peer_id = :id ORDER BY msg_date DESC")
    Flowable<List<AudioMessage>> byPeer(int id);
}
