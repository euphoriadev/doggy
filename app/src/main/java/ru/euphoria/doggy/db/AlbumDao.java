package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import ru.euphoria.doggy.api.model.Album;

@Dao
public interface AlbumDao extends BaseDao<Album> {
    @Query("SELECT * FROM albums WHERE audio_id = :audio")
    Album get(int audio);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReplace(Album album);
}