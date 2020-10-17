package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "albums")
public class Album implements Parcelable {
    public static final String NO_IMAGE = "no_img";

    @PrimaryKey
    public int audio_id;

    public String img;
    public String img_medium;
    public String img_max;

    public Album(int audio_id, String img, String img_medium, String img_max) {
        this.audio_id = audio_id;
        this.img = img;
        this.img_medium = img_medium;
        this.img_max = img_max;
    }


    public Album() {
        // empty
    }

    protected Album(Parcel in) {
        this.audio_id = in.readInt();
        this.img = in.readString();
        this.img_medium = in.readString();
        this.img_max = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(audio_id);
        dest.writeString(img);
        dest.writeString(img_medium);
        dest.writeString(img_max);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public int hashCode() {
        return Objects.hash(audio_id, img, img_medium, img_max);
    }
}