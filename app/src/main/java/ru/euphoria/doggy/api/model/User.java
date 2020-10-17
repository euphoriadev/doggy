package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.json.JSONObject;

import java.util.Objects;

import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.db.converter.LastSeenTypeConverter;

/**
 * Created by admin on 24.03.18.
 */

@Entity(tableName = "users")
public class User extends BaseModel implements Parcelable {
    /** Stated of deactivated value */
    public static final String DELETED = "deleted";
    public static final String BANNED = "banned";

    /** User sex, male or female */
    public static final int SEX_FEMALE = 1;
    public static final int SEX_MALE = 2;

    public static final String DEFAULT_FIELDS = "photo_50, photo_100, photo_200, sex, " +
            "nickname, bdate, status, screen_name, domain, online, online_mobile, last_seen, " +
            "verified, is_friend, deactivated, has_mobile, contacts, connections, birthday";

    /** User object with empty name; */
    public static final User EMPTY = new User() {
        @Override
        public String toString() {
            return "";
        }
    };

    /** User ID, positive number. */
    @PrimaryKey
    public int id;

    /** First name of user. */
    public String first_name;

    /** Last name of user. */
    public String last_name;

    /** User page's screen name (sub domain) */
    public String screen_name;

    /** Nickname of user */
    public String nickname;

    /** Gets the value deleted or banned */
    public String deactivated;

    /** Information whether the user is online. */
    public boolean online;

    /** If user utilizes a mobile application or site mobile version. */
    public boolean online_mobile;

    /** ID of mobile application, if user is online */
    public int online_app;

    /** URL of default square photo of the user with 50 pixels in width. */
    public String photo_50;

    /** URL of default square photo of the user with 100 pixels in width. */
    public String photo_100;

    /** URL of default square photo of the user with 200 pixels in width. */
    public String photo_200;

    /** Status of user */
    public String status;

    /** Returned in the format D.M.YYYY or D.M (if year of birth is hidden). */
    public String birthday;

    /** Last visit date in unix time */
    @TypeConverters(LastSeenTypeConverter.class)
    public LastSeen last_seen;

    /** True if the profile is verified, false if not */
    public boolean verified;

    /** User sex, 1 - female, 2 - male */
    public int sex;

    /** Information whether the user is a friend of current user */
    public boolean is_friend;

    /** Returns true — mobile phone number is available, false if not */
    public boolean has_mobile;

    /** is profile closed by privacy. */
    public boolean is_closed;

    /** Current user can see users profile when is_closed = true. */
    public boolean can_access_closed;

    /** Mobile phone number, additional phone number */
    public String mobile_phone, home_phone;

    public String skype, facebook, twitter, instagram;

    // for chat users
    @Ignore
    public int invited_by;

    /** list of the user's friend lists */
    @Ignore
    public int[] lists;

    // for database
    public long insert_time;

    public User() {

    }

    /**
     * Creates a new user model with fields from json source
     *
     * @param source the json source to parse
     */
    public User(JSONObject source) {
        this.id = source.optInt("id");
        this.first_name = source.optString("first_name", "DELETED");
        this.last_name = source.optString("last_name", "DELETED");
        this.nickname = source.optString("nickname");
        this.photo_50 = source.optString("photo_50");
        this.photo_100 = source.optString("photo_100");
        this.photo_200 = source.optString("photo_200");
        this.screen_name = source.optString("screen_name");
        this.online = source.optInt("online") == 1;
        this.status = source.optString("status");
        this.birthday = source.optString("bdate");
        this.deactivated = source.optString("deactivated");
        this.online_mobile = source.optInt("online_mobile") == 1;
        this.verified = source.optInt("verified") == 1;
        this.is_friend = source.optInt("is_friend") == 1;
        this.has_mobile = source.optInt("has_mobile") == 1;
        this.is_closed = source.optInt("is_closed") == 1;
        this.can_access_closed = source.optInt("can_access_closed") == 1;
        this.invited_by = source.optInt("invited_by");
        this.sex = source.optInt("sex");
        this.mobile_phone = source.optString("mobile_phone");
        this.home_phone = source.optString("home_phone");

        this.skype = source.optString("skype");
        this.facebook = source.optString("facebook");
        this.twitter = source.optString("twitter");
        this.instagram = source.optString("instagram");

        if (this.online_mobile) {
            this.online_app = source.optInt("online_app");
        }
        JSONObject lastSeen = source.optJSONObject("last_seen");
        if (lastSeen != null) {
            this.last_seen = new LastSeen(lastSeen);
        }

        this.lists = VKApi.parseArray(source.optJSONArray("lists"));
        this.insert_time = System.nanoTime();
    }

    protected User(Parcel in) {
        this.id = in.readInt();
        this.first_name = in.readString();
        this.last_name = in.readString();
        this.screen_name = in.readString();
        this.nickname = in.readString();
        this.deactivated = in.readString();
        this.online = in.readByte() != 0;
        this.online_mobile = in.readByte() != 0;
        this.online_app = in.readInt();
        this.photo_50 = in.readString();
        this.photo_100 = in.readString();
        this.photo_200 = in.readString();
        this.status = in.readString();
        this.birthday = in.readString();
        this.verified = in.readByte() != 0;
        this.sex = in.readInt();
        this.is_friend = in.readByte() != 0;
        this.has_mobile = in.readByte() != 0;
        this.is_closed = in.readByte() != 0;
        this.can_access_closed = in.readByte() != 0;
        this.mobile_phone = in.readString();
        this.home_phone = in.readString();
        this.skype = in.readString();
        this.facebook = in.readString();
        this.twitter = in.readString();
        this.instagram = in.readString();
        this.invited_by = in.readInt();
        this.lists = in.createIntArray();
        this.insert_time = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(screen_name);
        dest.writeString(nickname);
        dest.writeString(deactivated);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeByte((byte) (online_mobile ? 1 : 0));
        dest.writeInt(online_app);
        dest.writeString(photo_50);
        dest.writeString(photo_100);
        dest.writeString(photo_200);
        dest.writeString(status);
        dest.writeString(birthday);
        dest.writeByte((byte) (verified ? 1 : 0));
        dest.writeInt(sex);
        dest.writeByte((byte) (is_friend ? 1 : 0));
        dest.writeByte((byte) (has_mobile ? 1 : 0));
        dest.writeByte((byte) (is_closed ? 1 : 0));
        dest.writeByte((byte) (can_access_closed ? 1 : 0));
        dest.writeString(mobile_phone);
        dest.writeString(home_phone);
        dest.writeString(skype);
        dest.writeString(facebook);
        dest.writeString(twitter);
        dest.writeString(instagram);
        dest.writeInt(invited_by);
        dest.writeIntArray(lists);
        dest.writeLong(insert_time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return first_name + " " + last_name;
    }

    public static class LastSeen implements Parcelable {

        public long time;
        public int platform;

        public LastSeen() {
        }

        public LastSeen(JSONObject source) {
            this.time = source.optLong("time");
            this.platform = source.optInt("platform");
        }

        protected LastSeen(Parcel in) {
            time = in.readLong();
            platform = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(time);
            dest.writeInt(platform);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<LastSeen> CREATOR = new Creator<LastSeen>() {
            @Override
            public LastSeen createFromParcel(Parcel in) {
                return new LastSeen(in);
            }

            @Override
            public LastSeen[] newArray(int size) {
                return new LastSeen[size];
            }
        };
    }
}
