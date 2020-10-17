package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Gift implements Parcelable {
    /** User id who sent the gift, or 0 if the sender is hidden */
    public int from_id;

    /** Gift ID */
    public long id;

    /** Text of the message attached to the gift */
    public String message;

    /** Time to send gift in unix time format */
    public long date;

    /** URL image gift size 48x48px; */
    public String thumb_48;

    /** URL image gift size 96x96px; */
    public String thumb_96;

    /** URL image gift size 256x256px; */
    public String thumb_256;

    /**
     * Creates a new gift with fields from json source
     *
     * @param source the json source to parse
     */
    public Gift(JSONObject source) {
        this.id = source.optLong("id");
        this.from_id = source.optInt("from_id");
        this.date = source.optLong("date");

        if (source.has("gift")) {
            source = source.optJSONObject("gift");
        }

        this.thumb_48 = source.optString("thumb_48");
        this.thumb_96 = source.optString("thumb_96");
        this.thumb_256 = source.optString("thumb_256");
    }

    protected Gift(Parcel in) {
        from_id = in.readInt();
        id = in.readLong();
        message = in.readString();
        date = in.readLong();
        thumb_48 = in.readString();
        thumb_96 = in.readString();
        thumb_256 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(from_id);
        dest.writeLong(id);
        dest.writeString(message);
        dest.writeLong(date);
        dest.writeString(thumb_48);
        dest.writeString(thumb_96);
        dest.writeString(thumb_256);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Gift> CREATOR = new Creator<Gift>() {
        @Override
        public Gift createFromParcel(Parcel in) {
            return new Gift(in);
        }

        @Override
        public Gift[] newArray(int size) {
            return new Gift[size];
        }
    };
}