package ru.euphoria.doggy.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Audio;

@Dao
public interface AudiosDao extends BaseDao<Audio> {
    @Query("SELECT COUNT(*) FROM audios")
    int count();

    @Query("SELECT * FROM audios ORDER BY id DESC")
    LiveData<List<Audio>> all();

    @Query("SELECT * FROM audios WHERE owner_id = :id ORDER BY id DESC")
    LiveData<List<Audio>> byOwner(int id);

    @Query("SELECT * FROM audios WHERE id = :id LIMIT 1")
    Audio byId(int id);

    @Query("SELECT * FROM audios WHERE peer_id = :id ORDER BY msg_date DESC")
    Flowable<List<Audio>> byPeer(int id);
}