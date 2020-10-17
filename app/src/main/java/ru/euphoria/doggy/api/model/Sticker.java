package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Describes a sticker object from VK
 */

public class Sticker implements Parcelable {
    /** Sticker ID. */
    public int id;

    /** Set ID. */
    public int product_id;

    /** URL of the image with height of 64 px. */
    public String photo_64;

    /** URL of the image with height of 128 px. */
    public String photo_128;

    /** URL of the image with height of 256 px. */
    public String photo_256;

    /** URL of the image with height of 352 px. */
    public String photo_352;

    /** Height in px. */
    public int width;

    /** Height in px. */
    public int height;

    /**
     * Creates a new sticker model with fields from json source.
     *
     * @param source the json source to parse
     */
    public Sticker(JSONObject source) {
        this.id = source.optInt("id");
        this.product_id = source.optInt("product_id");
        this.photo_64 = source.optString("photo_64");
        this.photo_128 = source.optString("photo_128");
        this.photo_256 = source.optString("photo_256");
        this.photo_352 = source.optString("photo_352");
        this.width = source.optInt("width");
        this.height = source.optInt("height");
    }

    protected Sticker(Parcel in) {
        this.id = in.readInt();
        this.product_id = in.readInt();
        this.photo_64 = in.readString();
        this.photo_128 = in.readString();
        this.photo_256 = in.readString();
        this.photo_352 = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(product_id);
        dest.writeString(photo_64);
        dest.writeString(photo_128);
        dest.writeString(photo_256);
        dest.writeString(photo_352);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };
}