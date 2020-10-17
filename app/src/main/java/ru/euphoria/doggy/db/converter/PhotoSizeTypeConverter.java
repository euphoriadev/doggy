package ru.euphoria.doggy.db.converter;

import androidx.room.TypeConverter;

import ru.euphoria.doggy.api.model.PhotoSizes;
import ru.euphoria.doggy.util.AndroidUtil;

public class PhotoSizeTypeConverter {

    @TypeConverter
    public PhotoSizes fromBytes(byte[] array) {
        try {
            return (PhotoSizes) AndroidUtil.unmarshall(array, PhotoSizes.CREATOR);
        } catch (Exception e) {
            // ignores
        }
        return null;
    }

    @TypeConverter
    public byte[] fromSizes(PhotoSizes sizes) {
        try {
            return AndroidUtil.marshall(sizes);
        } catch (Exception e) {
            // ignored
        }
        return null;
    }

}
