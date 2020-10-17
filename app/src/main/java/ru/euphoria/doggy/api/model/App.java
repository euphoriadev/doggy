package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class App extends BaseModel implements Parcelable {
    /** Application ID. */
    public int id;

    /** Application title. */
    public String title;

    /**
     * Creates a new app model with fields from json source.
     *
     * @param source the json source to parse
     */
    public App(JSONObject source) {
        this.id = source.optInt("id");
        this.title = source.optString("title");
    }

    public App() {
        // empty
    }

    protected App(Parcel in) {
        id = in.readInt();
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };
}
