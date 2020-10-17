package ru.euphoria.doggy.db.converter;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

import ru.euphoria.doggy.api.VKApi;

public class IntArrayTypeConverter {

    @TypeConverter
    public int[] fromString(String source) {
        try {
            return VKApi.parseArray(new JSONArray(source));
        } catch (JSONException e) {
            return new int[0];
        }
    }

    @TypeConverter
    public String toString(int[] values) {
        return Arrays.toString(values);
    }
}
