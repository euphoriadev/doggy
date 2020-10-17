package ru.euphoria.doggy.itunes;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONObject;

public class SearchResult implements Parcelable {
    public static final String ALBUM_SIZE_SMALL = "200x200-150";
    public static final String ALBUM_SIZE_MEDIUM = "1000x1000-999";
    public static final String ALBUM_SIZE_MAX = "10000x10000-999";

    public String wrapperType;
    public String trackName;
    public String artistName;
    public String artworkUrl100;

    public SearchResult(JSONObject source) {
        this.wrapperType = source.optString("wrapperType");
        this.trackName = source.optString("trackName");
        this.artistName = source.optString("artistName");
        this.artworkUrl100 = source.optString("artworkUrl100");
    }

    protected SearchResult(Parcel in) {
        wrapperType = in.readString();
        trackName = in.readString();
        artistName = in.readString();
        artworkUrl100 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(wrapperType);
        dest.writeString(trackName);
        dest.writeString(artistName);
        dest.writeString(artworkUrl100);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchResult> CREATOR = new Creator<SearchResult>() {
        @Override
        public SearchResult createFromParcel(Parcel in) {
            return new SearchResult(in);
        }

        @Override
        public SearchResult[] newArray(int size) {
            return new SearchResult[size];
        }
    };

    public String buildMaxAlbumUrl() {
        return buildAlbumUrl(ALBUM_SIZE_MAX);
    }

    public String buildAlbumUrl(String size) {
        if (TextUtils.isEmpty(artworkUrl100)) return "";

        String x = artworkUrl100;
        return x.substring(0, x.lastIndexOf("/") + 1) + size + ".jpg";
    }
}