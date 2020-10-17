package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "monitor_events")
public class LongPollEvent implements Parcelable {
    public static final int EVENT_CHANGE_FLAGS = 2;
    public static final int EVENT_NEW_MESSAGE = 4;
    public static final int EVENT_READ_MESSAGE_IN = 6;
    public static final int EVENT_READ_MESSAGE_OUT = 7;
    public static final int EVENT_USER_ONLINE = 8;
    public static final int EVENT_USER_OFFLINE = 9;
    public static final int EVENT_TYPING_TEXT = 61;
    public static final int EVENT_TYPING_VOICE = 64;

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int code;
    public long time;
    public String updates;

    @Ignore
    public User user;

    @Ignore
    public Message msg;

    @Ignore
    public String text;

    public LongPollEvent(int code, long time, String updates) {
        this.code = code;
        this.time = time;
        this.updates = updates;
    }

    protected LongPollEvent(Parcel in) {
        id = in.readInt();
        code = in.readInt();
        time = in.readLong();
        updates = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        msg = in.readParcelable(Message.class.getClassLoader());
        text = in.readString();
    }

    public static final Creator<LongPollEvent> CREATOR = new Creator<LongPollEvent>() {
        @Override
        public LongPollEvent createFromParcel(Parcel in) {
            return new LongPollEvent(in);
        }

        @Override
        public LongPollEvent[] newArray(int size) {
            return new LongPollEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(code);
        dest.writeLong(time);
        dest.writeString(updates);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(msg, flags);
        dest.writeString(text);
    }
}
