package ru.euphoria.doggy.db.converter;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import ru.euphoria.doggy.api.model.User;

public class LastSeenTypeConverter {

    @TypeConverter
    public User.LastSeen fromString(String source) {
        User.LastSeen lastSeen = new User.LastSeen();
        if (TextUtils.isEmpty(source)) return lastSeen;

        String[] split = source.split(":");
        lastSeen.time = Integer.parseInt(split[0]);
        lastSeen.platform = Integer.parseInt(split[1]);

        return lastSeen;
    }

    @TypeConverter
    public String fromLastSeen(User.LastSeen source) {
        if (source == null) return "";

        StringBuilder buffer = new StringBuilder();
        buffer.append(source.time)
                .append(":")
                .append(source.platform);
        return buffer.toString();
    }
}
