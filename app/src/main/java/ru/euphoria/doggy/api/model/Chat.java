package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import ru.euphoria.doggy.api.VKApi;

public class Chat implements Parcelable {
    private static final long serialVersionUID = 1L;

    /** Chat ID. */
    public int id;

    /** Dialog type, e.g. chat */
    public String type;

    /** Chat title */
    public String title;

    /** ID of the chat starter. */
    public int admin_id;

    /** List of chat participants' IDs (integer). */
    public int[] users;

    /** Count of members in chat */
    public int members_count;

    /** URL chat photo with 50 px in width (if available) */
    public String photo_50;

    /** URL chat photo with 100 px in width (if available) */
    public String photo_100;

    /** URL chat photo with 200 px in width (if available) */
    public String photo_200;

    /** Shows that user has been left the chat. Always contains true. */
    public boolean left;

    /** Shows that user has been kicked from the chat. Always contains true. */
    public boolean kicked;

    public Chat() {
        // empty
    }

    /**
     * Creates a new photo model with fields from json source
     *
     * @param source the json source to parse
     */
    public Chat(JSONObject source) {
        this.id = source.optInt("id");
        this.type = source.optString("type");
        this.title = source.optString("title");
        this.admin_id = source.optInt("admin_id");
        this.users = VKApi.parseArray(source.optJSONArray("users"));
        this.members_count = source.optInt("members_count");
        this.photo_50 = source.optString("photo_50");
        this.photo_100 = source.optString("photo_100");
        this.photo_200 = source.optString("photo_200");
        this.kicked = source.optInt("kicked") == 1;
        this.left = source.optInt("left") == 1;
    }

    protected Chat(Parcel in) {
        id = in.readInt();
        type = in.readString();
        title = in.readString();
        admin_id = in.readInt();
        users = in.createIntArray();
        members_count = in.readInt();
        photo_50 = in.readString();
        photo_100 = in.readString();
        photo_200 = in.readString();
        left = in.readByte() != 0;
        kicked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeInt(admin_id);
        dest.writeIntArray(users);
        dest.writeInt(members_count);
        dest.writeString(photo_50);
        dest.writeString(photo_100);
        dest.writeString(photo_200);
        dest.writeByte((byte) (left ? 1 : 0));
        dest.writeByte((byte) (kicked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return title;
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };
}
