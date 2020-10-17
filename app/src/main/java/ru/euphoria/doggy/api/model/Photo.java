package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import ru.euphoria.doggy.db.converter.PhotoSizeTypeConverter;

import static ru.euphoria.doggy.api.model.Attachments.TYPE_PHOTO;

/**
 * Describes a photo object from VK.
 */
@Entity(tableName = "photos")
public class Photo extends Attachment implements Parcelable, ClusterItem {

    /** Sort by newest date */
    public static final Comparator<Photo> DEFAULT_COMPARATOR =
            (o1, o2) -> Long.compare(o2.date, o1.date);

    /** Sort by oldest date */
    public static final Comparator<Photo> DEFAULT_COMPARATOR_REVERSE =
            (o1, o2) -> Long.compare(o1.date, o2.date);

    /** Photo ID, positive number */
    @PrimaryKey
    public int id;

    /** Photo album ID. */
    public int album_id;

    /** ID of the user or community that owns the photo. */
    public int owner_id;

    /**
     * ID of the user who uploaded the photo (if the photo is uploaded in community).
     * 100 for photos uploaded by the community
     */
    public int user_id;

    /** Width (in pixels) of the original photo. */
    public int width;

    /** Height (in pixels) of the original photo. */
    public int height;

    /** Text describing the photo. */
    public String text;

    /** Date (in Unix time) the photo was added. */
    @ColumnInfo(index = true)
    public long date;

    /** Information whether the current user liked the photo. */
    public boolean user_likes;

    /** Whether the current user can comment on the photo */
    public boolean can_comment;

    /** Number of likes on the photo. */
    public int likes;

    /** Number of comments on the photo. */
    public int comments;

    /** Number of tags on the photo. */
    public int tags;

    /** An access key using for get information about hidden objects. */
    public String access_key;

    /** Array with the photo copies of different sizes */
    @TypeConverters(PhotoSizeTypeConverter.class)
    public PhotoSizes sizes;

    /** Indicates when photo has lat and long geo */
    public boolean has_geo;

    public double lat;
    public double lng;

    public Photo() {
        // empty
    }

    protected Photo(Parcel in) {
        super(in);
        id = in.readInt();
        album_id = in.readInt();
        owner_id = in.readInt();
        user_id = in.readInt();
        width = in.readInt();
        height = in.readInt();
        text = in.readString();
        date = in.readLong();
        user_likes = in.readByte() != 0;
        can_comment = in.readByte() != 0;
        likes = in.readInt();
        comments = in.readInt();
        tags = in.readInt();
        access_key = in.readString();
        sizes = in.readParcelable(PhotoSizes.class.getClassLoader());
        has_geo = in.readByte() != 0;
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(album_id);
        dest.writeInt(owner_id);
        dest.writeInt(user_id);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(text);
        dest.writeLong(date);
        dest.writeByte((byte) (user_likes ? 1 : 0));
        dest.writeByte((byte) (can_comment ? 1 : 0));
        dest.writeInt(likes);
        dest.writeInt(comments);
        dest.writeInt(tags);
        dest.writeString(access_key);
        dest.writeParcelable(sizes, flags);
        dest.writeByte((byte) (has_geo ? 1 : 0));
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public String toAttachmentString() {
        StringBuilder result = new StringBuilder(TYPE_PHOTO)
                .append(owner_id)
                .append('_')
                .append(id);
        if (!TextUtils.isEmpty(access_key)) {
            result.append('_');
            result.append(access_key);
        }
        return result.toString();
    }

    /**
     * Creates a new photo model with fields from json source
     *
     * @param source the json source to parse
     */
    public Photo(JSONObject source) {
        this.id = source.optInt("id");
        this.owner_id = source.optInt("owner_id");
        this.album_id = source.optInt("album_id");
        this.date = source.optLong("date");
        this.width = source.optInt("width");
        this.height = source.optInt("height");
        this.text = source.optString("text");
        this.access_key = source.optString("access_key");
        this.can_comment = source.optInt("can_comment") == 1;

        if (source.has("sizes")) {
            this.sizes = new PhotoSizes(source.optJSONArray("sizes"));
        }


        if (source.has("lat") && source.has("long")) {
            this.has_geo = true;
            this.lat = source.optDouble("lat", 0);
            this.lng = source.optDouble("long", 0);
        }

        JSONObject likes = source.optJSONObject("likes");
        if (likes != null) {
            this.likes = likes.optInt("count");
            this.user_likes = likes.optInt("user_likes") == 1;
        }
        JSONObject comments = source.optJSONObject("comments");
        if (comments != null) {
            this.comments = comments.optInt("count");
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "size: %dx%d, sizes: %s", width, height,
                Arrays.toString(sizes.toArray()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return id == photo.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(lat, lng);
    }

    @Override
    public String getTitle() {
        return text;
    }

    @Override
    public String getSnippet() {
        return null;
    }


}