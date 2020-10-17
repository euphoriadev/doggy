package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;

import ru.euphoria.doggy.util.ArrayUtil;

@Entity(tableName = "calls")
public class Call extends Attachment implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int initiator_id;
    public int receiver_id;
    public String state;
    public int time;
    public int duration;

    public Call() {
        // empty
    }

    public Call(JSONObject source) {
        this.initiator_id = source.optInt("initiator_id");
        this.receiver_id = source.optInt("receiver_id");
        this.state = source.optString("state");
        this.time = source.optInt("time");
        this.duration = source.optInt("duration");
        this.id = ArrayUtil.hash(initiator_id, receiver_id, time);
    }

    protected Call(Parcel in) {
        super(in);
        id = in.readInt();
        initiator_id = in.readInt();
        receiver_id = in.readInt();
        state = in.readString();
        time = in.readInt();
        duration = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(initiator_id);
        dest.writeInt(receiver_id);
        dest.writeString(state);
        dest.writeInt(time);
        dest.writeInt(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Call> CREATOR = new Creator<Call>() {
        @Override
        public Call createFromParcel(Parcel in) {
            return new Call(in);
        }

        @Override
        public Call[] newArray(int size) {
            return new Call[size];
        }
    };
}
