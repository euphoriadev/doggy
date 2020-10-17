package ru.euphoria.doggy.db;

import androidx.room.Dao;

import ru.euphoria.doggy.api.model.Call;

@Dao
public interface CallsDao extends BaseDao<Call> {
}
