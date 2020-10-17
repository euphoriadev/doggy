package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.json.JSONObject;

import ru.euphoria.doggy.db.converter.PhotoTypeConverter;

/**
 * A link object describes a link attachment
 */
@Entity(tableName = "links")
public class Link extends Attachment implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    /** Link URL */
    public String url;

    /** Link title */
    public String title;

    /** Link caption (if any). */
    public String caption;

    /** Link description; */
    public String description;

    /** Whether the link is external (1 — true, 0 — false). */
    public boolean is_external;

    /** Image preview URL for the link (if any). */
    @TypeConverters(PhotoTypeConverter.class)
    public Photo photo;

    /**
     * ID wiki page with content for the preview of the page contents
     * ID is returned as "ownerid_pageid".
     */
    public String preview_page;


    /** URL of the page for preview */
    public String preview_url;

    public Link(JSONObject source) {
        this.url = source.optString("url");
        this.title = source.optString("title");
        this.caption = source.optString("caption");
        this.description = source.optString("description");
        this.is_external = source.optInt("is_external") == 1;
        this.preview_page = source.optString("preview_page");
        this.preview_url = source.optString("preview_url");

        JSONObject jsonPhoto = source.optJSONObject("photo");
        if (jsonPhoto != null) {
            this.photo =  new Photo(jsonPhoto);
        }
    }

    /**
     * Creates empty Link instance.
     */
    public Link() {

    }

    protected Link(Parcel in) {
        super(in);
        id = in.readInt();
        url = in.readString();
        title = in.readString();
        caption = in.readString();
        description = in.readString();
        is_external = in.readByte() != 0;
        photo = in.readParcelable(Photo.class.getClassLoader());
        preview_page = in.readString();
        preview_url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(caption);
        dest.writeString(description);
        dest.writeByte((byte) (is_external ? 1 : 0));
        dest.writeParcelable(photo, flags);
        dest.writeString(preview_page);
        dest.writeString(preview_url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Link> CREATOR = new Creator<Link>() {
        @Override
        public Link createFromParcel(Parcel in) {
            return new Link(in);
        }

        @Override
        public Link[] newArray(int size) {
            return new Link[size];
        }
    };
}