package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;

import static ru.euphoria.doggy.api.model.Attachments.TYPE_VIDEO;

/**
 * A video object describes an video file.
 */
@Entity(tableName = "videos")
public class Video extends Attachment implements Parcelable {

    /** Video ID. */
    @PrimaryKey
    public int id;

    /** Video owner ID. */
    public int owner_id;

    /** Video album ID. */
    public int album_id;

    /** Video title. */
    public String title;

    /** Text describing video. */
    public String description;

    /** Duration of the video in seconds. */
    public int duration;

    /** String with video+vid key. */
    public String link;

    /** Date when the video was added, as unix time. */
    public long date;

    /** Number of views of the video. */
    public int views;

    /**
     * URL of the page with a player that can be used to play a video in the browser.
     * Flash and HTML5 video players are supported; the player is always zoomed to fit
     * the window size.
     */
    public String player;

    /** URL of the video cover image with the size of 130x98px. */
    public String photo_130;

    /** URL of the video cover image with the size of 320x240px. */
    public String photo_320;

    /** URL of the video cover image with the size of 640x480px (if available). */
    public String photo_640;

    /** An access key using for get information about hidden objects. */
    public String access_key;

    /** Number of comments on the video. */
    public int comments;

    /** Whether the current user can comment on the video */
    public boolean can_comment;

    /** Whether the current user can re-post this video */
    public boolean can_repost;

    /** Information whether the current user liked the video. */
    public boolean user_likes;

    /** Information whether the the video should be repeated. */
    public boolean repeat;

    /** Number of likes on the video. */
    public int likes;

    /** Privacy to view of this video. */
    public int privacy_view;

    /** Privacy to comment of this video. */
    public int privacy_comment;

    /** URL of video with height of 240 pixels. Returns only if you use direct auth. */
    public String mp4_240;

    /** URL of video with height of 360 pixels. Returns only if you use direct auth. */
    public String mp4_360;

    /** URL of video with height of 480 pixels. Returns only if you use direct auth. */
    public String mp4_480;

    /** URL of video with height of 720 pixels. Returns only if you use direct auth. */
    public String mp4_720;

    /** URL of video with height of 1080 pixels. Returns only if you use direct auth. */
    public String mp4_1080;

    /** URL of the external video link. */
    public String external;

    /**
     * Creates a new video model with fields from json source
     *
     * @param source the json source to parse
     */
    public Video(JSONObject source) {
        this.id = source.optInt("id");
        this.owner_id = source.optInt("owner_id");
        this.title = source.optString("title");
        this.description = source.optString("description");
        this.duration = source.optInt("duration");
        this.link = source.optString("link");
        this.date = source.optLong("date");
        this.views = source.optInt("views");
        this.comments = source.optInt("comments");
        this.player = source.optString("player");
        this.access_key = source.optString("access_key");
        this.album_id = source.optInt("album_id");

        this.photo_130 = source.optString("photo_130");
        this.photo_320 = source.optString("photo_320");
        this.photo_640 = source.optString("photo_640");

        JSONObject likes = source.optJSONObject("likes");
        if (likes != null) {
            this.likes = likes.optInt("count");
            this.user_likes = likes.optInt("user_likes") == 1;
        }
        this.can_comment = source.optInt("can_comment") == 1;
        this.can_repost = source.optInt("can_repost") == 1;
        this.repeat = source.optInt("repeat") == 1;

        JSONObject files = source.optJSONObject("files");
        if (files != null) {
            this.mp4_240 = files.optString("mp4_240");
            this.mp4_360 = files.optString("mp4_360");
            this.mp4_480 = files.optString("mp4_480");
            this.mp4_720 = files.optString("mp4_720");
            this.mp4_1080 = files.optString("mp4_1080");
            this.external = files.optString("external");
        }
    }

    public Video() {
        // empty
    }

    protected Video(Parcel in) {
        super(in);
        this.id = in.readInt();
        this.owner_id = in.readInt();
        this.album_id = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
        this.duration = in.readInt();
        this.link = in.readString();
        this.date = in.readLong();
        this.views = in.readInt();
        this.player = in.readString();
        this.photo_130 = in.readString();
        this.photo_320 = in.readString();
        this.photo_640 = in.readString();
        this.access_key = in.readString();
        this.comments = in.readInt();
        this.can_comment = in.readByte() != 0;
        this.can_repost = in.readByte() != 0;
        this.user_likes = in.readByte() != 0;
        this.repeat = in.readByte() != 0;
        this.likes = in.readInt();
        this.privacy_view = in.readInt();
        this.privacy_comment = in.readInt();
        this.mp4_240 = in.readString();
        this.mp4_360 = in.readString();
        this.mp4_480 = in.readString();
        this.mp4_720 = in.readString();
        this.mp4_1080 = in.readString();
        this.external = in.readString();
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeInt(album_id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(duration);
        dest.writeString(link);
        dest.writeLong(date);
        dest.writeInt(views);
        dest.writeString(player);
        dest.writeString(photo_130);
        dest.writeString(photo_320);
        dest.writeString(photo_640);
        dest.writeString(access_key);
        dest.writeInt(comments);
        dest.writeByte((byte) (can_comment ? 1 : 0));
        dest.writeByte((byte) (can_repost ? 1 : 0));
        dest.writeByte((byte) (user_likes ? 1 : 0));
        dest.writeByte((byte) (repeat ? 1 : 0));
        dest.writeInt(likes);
        dest.writeInt(privacy_view);
        dest.writeInt(privacy_comment);
        dest.writeString(mp4_240);
        dest.writeString(mp4_360);
        dest.writeString(mp4_480);
        dest.writeString(mp4_720);
        dest.writeString(mp4_1080);
        dest.writeString(external);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    @Override
    public String toAttachmentString() {
        StringBuilder result = new StringBuilder(TYPE_VIDEO)
                .append(owner_id)
                .append('_')
                .append(id);
        if (!TextUtils.isEmpty(access_key)) {
            result.append('_');
            result.append(access_key);
        }
        return result.toString();
    }
}