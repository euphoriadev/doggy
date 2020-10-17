package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.euphoria.doggy.db.converter.PhotoSizeTypeConverter;

/**
 * Describes a document object from VK.
 */
@Entity(tableName = "docs")
public class Document extends Attachment implements Parcelable {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_ARCHIVE = 2;
    public static final int TYPE_GIF = 3;
    public static final int TYPE_IMAGE = 4;
    public static final int TYPE_AUDIO = 5;
    public static final int TYPE_VIDEO = 6;
    public static final int TYPE_BOOK = 7;
    public static final int TYPE_UNKNOWN = 8;

    /** Document ID. */
    @PrimaryKey
    public long id;

    /** ID of the user or group who uploaded the document. */
    public long owner_id;

    /** Document title. */
    public String title;

    /** Document size (in bytes). */
    public long size;

    /** Document extension. */
    public String ext;

    /** Document URL for downloading. */
    public String url;

    /** URL of the 100x75px image (if the file is graphical). */
    public String photo_100;

    /** URL of the 130x100px image (if the file is graphical). */
    public String photo_130;

    /** An access key using for get information about hidden objects. */
    public String access_key;

    /** The document type (audio, video, book) */
    public int type;

    /** Date (in Unix time) the doc was added. */
    public long date;

    /** Images for preview */
    @TypeConverters(PhotoSizeTypeConverter.class)
    public PhotoSizes photo_sizes;

    public Document() {

    }

    /**
     * Creates a new document model with fields from json source.
     *
     * @param source the json source to parse
     */
    public Document(JSONObject source) {
        this.id = source.optLong("id");
        this.owner_id = source.optLong("owner_id");
        this.title = source.optString("title");
        this.url = source.optString("url");
        this.size = source.optLong("size");
        this.type = source.optInt("type");
        this.ext = source.optString("ext");
        this.date = source.optLong("date");
        this.photo_130 = source.optString("photo_130");
        this.photo_100 = source.optString("photo_100");
        this.access_key = source.optString("access_key");
        this.type = source.optInt("type");

        JSONObject preview = source.optJSONObject("preview");
        if (preview != null && preview.has("photo")) {
            JSONArray sizes = preview.optJSONObject("photo")
                    .optJSONArray("sizes");

            photo_sizes = new PhotoSizes(sizes);
        }
    }

    protected Document(Parcel in) {
        super(in);
        id = in.readLong();
        owner_id = in.readLong();
        title = in.readString();
        size = in.readLong();
        ext = in.readString();
        url = in.readString();
        photo_100 = in.readString();
        photo_130 = in.readString();
        access_key = in.readString();
        type = in.readInt();
        date = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(id);
        dest.writeLong(owner_id);
        dest.writeString(title);
        dest.writeLong(size);
        dest.writeString(ext);
        dest.writeString(url);
        dest.writeString(photo_100);
        dest.writeString(photo_130);
        dest.writeString(access_key);
        dest.writeInt(type);
        dest.writeLong(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return title;
    }

    public static final Creator<Document> CREATOR = new Creator<Document>() {
        @Override
        public Document createFromParcel(Parcel in) {
            return new Document(in);
        }

        @Override
        public Document[] newArray(int size) {
            return new Document[size];
        }
    };
}