package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;

public class Attachment implements Parcelable {

    // for search in database by chat
    @ColumnInfo(index = true)
    public int peer_id;
    @ColumnInfo(index = true)
    public int msg_id;

    // у пересланных сообщений нет id.
    // а сортировка по времени нужна, поэтому костыль
    @ColumnInfo(index = true)
    public long msg_date;

    public Attachment() {

    }

    protected Attachment(Parcel in) {
        peer_id = in.readInt();
        msg_id = in.readInt();
    }

    public String toAttachmentString() {
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(peer_id);
        dest.writeInt(msg_id);
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
}
