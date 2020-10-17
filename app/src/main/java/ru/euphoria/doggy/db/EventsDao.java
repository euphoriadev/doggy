package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import ru.euphoria.doggy.api.model.LongPollEvent;

@Dao
public interface EventsDao extends BaseDao<LongPollEvent> {
    @Query("SELECT * FROM monitor_events")
    List<LongPollEvent> all();
}
