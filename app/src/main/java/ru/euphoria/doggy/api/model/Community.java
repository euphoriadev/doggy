package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;

@Entity(tableName = "groups")
public class Community implements Parcelable {
    private final static String TYPE_GROUP = "group";
    private final static String TYPE_PAGE = "page";
    private final static String TYPE_EVENT = "event";

    /** Community ID. */
    @PrimaryKey
    public int id;

    /** Community name. */
    public String name;

    /** Screen name of the community page (e.g. apiclub or club1).. */
    public String screen_name;

    /** Whether the community is closed (0 — open, 1 — closed, 2 — private).. */
    public int is_closed;

    /**
     * Returns if the community is deleted or blocked.
     * Gets the value deleted or banned.
     * Keep in mind that in this case no additional fields are returned.
     */
    public String deactivated;

    /**
     * Whether a user is the community manager
     * (false — is not the manager, true — is the manager.
     */
    public boolean is_admin;

    /**
     * Rights of the user.
     *
     * @see AdminLevel
     */
    public int admin_level;

    /**
     * Community type.
     *
     * @see Type
     */
    public int type;

    /** URL of the 50px-wide community logo. */
    public String photo_50;

    /** URL of the 100px-wide community logo. */
    public String photo_100;

    /** URL of the 200px-wide community logo. */
    public String photo_200;

    /** The members count in group */
    public int members_count;

    public Community() {
        // empty
    }

    /**
     * Creates a new community model with fields from json source.
     *
     * @param source the json source to parse
     */
    public Community(JSONObject source) {
        this.id = source.optInt("id");
        this.name = source.optString("name");
        this.screen_name = source.optString("screen_name");
        this.is_closed = source.optInt("is_closed");
        this.is_admin = source.optInt("is_admin") == 1;
        this.admin_level = source.optInt("admin_level");
        this.deactivated = source.optString("deactivated");
        this.photo_50 = source.optString("photo_50");
        this.photo_100 = source.optString("photo_100");
        this.photo_200 = source.optString("photo_200");
        this.members_count = source.optInt("members_count");

        String type = source.optString("type", "group");
        switch (type) {
            case TYPE_GROUP:
                this.type = Type.GROUP;
                break;
            case TYPE_PAGE:
                this.type = Type.PAGE;
                break;
            case TYPE_EVENT:
                this.type = Type.EVENT;
                break;
        }
    }

    protected Community(Parcel in) {
        id = in.readInt();
        name = in.readString();
        screen_name = in.readString();
        is_closed = in.readInt();
        deactivated = in.readString();
        is_admin = in.readByte() != 0;
        admin_level = in.readInt();
        type = in.readInt();
        photo_50 = in.readString();
        photo_100 = in.readString();
        photo_200 = in.readString();
        members_count = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(screen_name);
        dest.writeInt(is_closed);
        dest.writeString(deactivated);
        dest.writeByte((byte) (is_admin ? 1 : 0));
        dest.writeInt(admin_level);
        dest.writeInt(type);
        dest.writeString(photo_50);
        dest.writeString(photo_100);
        dest.writeString(photo_200);
        dest.writeInt(members_count);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }

    public static final Creator<Community> CREATOR = new Creator<Community>() {
        @Override
        public Community createFromParcel(Parcel in) {
            return new Community(in);
        }

        @Override
        public Community[] newArray(int size) {
            return new Community[size];
        }
    };

    /** Access level to manage community. */
    public static class AdminLevel {
        private AdminLevel() {
        }

        public final static int MODERATOR = 1;
        public final static int EDITOR = 2;
        public final static int ADMIN = 3;
    }

    /** Privacy status of the group. */
    public static class Status {
        private Status() {
        }

        public final static int OPEN = 0;
        public final static int CLOSED = 1;
        public final static int PRIVATE = 2;
    }

    /** Types of communities. */
    public static class Type {
        private Type() {
        }

        public final static int GROUP = 0;
        public final static int PAGE = 1;
        public final static int EVENT = 2;
    }

}
