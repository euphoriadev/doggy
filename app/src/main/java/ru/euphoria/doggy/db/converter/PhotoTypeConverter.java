package ru.euphoria.doggy.db.converter;

import androidx.room.TypeConverter;

import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.util.AndroidUtil;

public class PhotoTypeConverter {

    @TypeConverter
    public Photo fromBytes(byte[] array) {
        try {
            return (Photo) AndroidUtil.unmarshall(array, Photo.CREATOR);
        } catch (Exception e) {
            // ignores
        }
        return null;
    }

    @TypeConverter
    public byte[] fromPhoto(Photo sizes) {
        try {
            return AndroidUtil.marshall(sizes);
        } catch (Exception e) {
            // ignored
        }
        return null;
    }

}