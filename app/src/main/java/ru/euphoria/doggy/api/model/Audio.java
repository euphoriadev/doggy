package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;

import java.util.Objects;

@Entity(tableName = "audios")
public class Audio extends Attachment implements Parcelable {
    /** Audio ID. */
    @PrimaryKey
    public int id;

    /** Audio owner ID. */
    @ColumnInfo(index = true)
    public int owner_id;

    /** Artist name. */
    public String artist;

    /** Audio file title. */
    public String title;

    /** Duration (in seconds). */
    public int duration;

    /** Link to mp3. */
    public String url;

    /** ID of the lyrics (if available) of the audio file. */
    public int lyrics_id;

    /** ID of the album containing the audio file (if assigned). */
    public int album_id;

    /** Genre ID. See the list of audio genres. */
    public int genre;

    /** An access key using for get information about hidden objects. */
    public String access_key;

    public Audio() {

    }

    /**
     * Creates a new audio model with fields from json source.
     *
     * @param source the json source to parse
     */
    public Audio(JSONObject source) {
        this.id = source.optInt("id");
        this.owner_id = source.optInt("owner_id");
        this.artist = source.optString("artist");
        this.title = source.optString("title");
        this.duration = source.optInt("duration");
        this.url = source.optString("url");
        this.lyrics_id = source.optInt("lyrics_id");
        this.album_id = source.optInt("album_id");
        this.genre = source.optInt("genre_id");
        this.access_key = source.optString("access_key");
    }

    protected Audio(Parcel in) {
        this.id = in.readInt();
        this.owner_id = in.readInt();
        this.artist = in.readString();
        this.title = in.readString();
        this.duration = in.readInt();
        this.url = in.readString();
        this.lyrics_id = in.readInt();
        this.album_id = in.readInt();
        this.genre = in.readInt();
        this.access_key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeInt(duration);
        dest.writeString(url);
        dest.writeInt(lyrics_id);
        dest.writeInt(album_id);
        dest.writeInt(genre);
        dest.writeString(access_key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }

    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audio audio = (Audio) o;
        return id == audio.id &&
                owner_id == audio.owner_id &&
                duration == audio.duration &&
                lyrics_id == audio.lyrics_id &&
                album_id == audio.album_id &&
                genre == audio.genre &&
                Objects.equals(artist, audio.artist) &&
                Objects.equals(title, audio.title) &&
                Objects.equals(url, audio.url) &&
                Objects.equals(access_key, audio.access_key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner_id, artist, title, duration, url, lyrics_id, album_id, genre, access_key);
    }


    /** Audio object genres. */
    public final static class Genre {
        private Genre() {
        }

        public final static int ROCK = 1;
        public final static int POP = 2;
        public final static int RAP_AND_HIPHOP = 3;
        public final static int EASY_LISTENING = 4;
        public final static int DANCE_AND_HOUSE = 5;
        public final static int INSTRUMENTAL = 6;
        public final static int METAL = 7;
        public final static int DUBSTEP = 8;
        public final static int JAZZ_AND_BLUES = 9;
        public final static int DRUM_AND_BASS = 10;
        public final static int TRANCE = 11;
        public final static int CHANSON = 12;
        public final static int ETHNIC = 13;
        public final static int ACOUSTIC_AND_VOCAL = 14;
        public final static int REGGAE = 15;
        public final static int CLASSICAL = 16;
        public final static int INDIE_POP = 17;
        public final static int OTHER = 18;
        public final static int SPEECH = 19;
        public final static int ALTERNATIVE = 21;
        public final static int ELECTROPOP_AND_DISCO = 22;
    }

}
