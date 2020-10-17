package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import ru.euphoria.doggy.util.MessageStats;

@Dao
public interface MessageStatsDao {
    @Query("SELECT * FROM messages_stats WHERE peer = :id")
    MessageStats byPeer(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MessageStats stats);
}
