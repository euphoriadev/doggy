package ru.euphoria.doggy.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import java.util.List;

@Dao
public interface BaseDao<E> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<E> values);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(E value);

    @Delete
    void delete(List<E> values);

    @Delete
    void delete(E value);
}
