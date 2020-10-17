package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Wall implements Parcelable {

    public Wall(JSONObject source) {

    }

    protected Wall(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Wall> CREATOR = new Creator<Wall>() {
        @Override
        public Wall createFromParcel(Parcel in) {
            return new Wall(in);
        }

        @Override
        public Wall[] newArray(int size) {
            return new Wall[size];
        }
    };
}
