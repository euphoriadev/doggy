package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Model to parse a list of photo with and height.
 * <p>
 * See https://vk.com/dev/objects/photo_sizes
 */
public class PhotoSizes extends ArrayList<PhotoSizes.PhotoSize>
        implements Parcelable, Iterable<PhotoSizes.PhotoSize> {
    /**
     * Creates a new photo sizes model with fields from json source.
     *
     * @param array the json array to parse
     */
    public PhotoSizes(JSONArray array) {
        super();

        ensureCapacity(array.length());
        for (int i = 0; i < array.length(); i++) {
            add(new PhotoSize(array.optJSONObject(i)));
        }
    }

    @SuppressWarnings("unchecked")
    protected PhotoSizes(Parcel in) {
        in.readTypedList(this, PhotoSize.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PhotoSizes> CREATOR = new Creator<PhotoSizes>() {
        @Override
        public PhotoSizes createFromParcel(Parcel in) {
            return new PhotoSizes(in);
        }

        @Override
        public PhotoSizes[] newArray(int size) {
            return new PhotoSizes[size];
        }
    };

    public PhotoSize of(char type) {
        for (PhotoSize size : this) {
            if (size.type == type) {
                return size;
            }
        }

        return null;
    }

    public PhotoSize small() {
        return of(PhotoSize.S);
    }

    public PhotoSize medium() {
        return of(PhotoSize.M);
    }

    public PhotoSize large() {
        return of(PhotoSize.X);
    }

    public static class PhotoSize implements Parcelable {
        /** Proportional copy with 75px max width. */
        public final static char S = 's';

        /** Proportional copy with 130px max width. */
        public final static char M = 'm';

        /** Proportional copy with 604px max width. */
        public final static char X = 'x';

        /** Proportional copy with 807px max width. */
        public final static char YY = 'y';

        /** Proportional copy with 1280x1024px max size. */
        public final static char Z = 'z';

        /** Proportional copy with 2560x2048px max size. */
        public final static char W = 'w';

        /**
         * If original image's "width/height" ratio is less or equal to 3:2, then proportional
         * copy with 130px max width. If original image's "width/height" ratio is more than 3:2,
         * then copy of cropped by left side image with 130px max width and 3:2 sides ratio.
         */
        public final static char O = 'o';

        /**
         * If original image's "width/height" ratio is less or equal to 3:2, then proportional
         * copy with 200px max width. If original image's "width/height" ratio is more than 3:2,
         * then copy of cropped by left side image with 200px max width and 3:2 sides ratio.
         */
        public final static char P = 'p';

        /**
         * If original image's "width/height" ratio is less or equal to 3:2, then proportional
         * copy with 320px max width. If original image's "width/height" ratio is more than 3:2,
         * then copy of cropped by left side image with 320px max width and 3:2 sides ratio.
         */
        public final static char Q = 'q';

        /** URL of image copy */
        public String src;

        /** Width (in pixels) of image copy */
        public int width;

        /** Height (in pixels) of image copy */
        public int height;

        /** Notation for copy size and ratio */
        public char type;

        public PhotoSize(JSONObject source) {
            this.src = getSrc(source);
            this.width = source.optInt("width");
            this.height = source.optInt("height");
            this.type = source.optString("type").charAt(0);
        }

        protected PhotoSize(Parcel in) {
            src = in.readString();
            width = in.readInt();
            height = in.readInt();
            type = (char) in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(src);
            dest.writeInt(width);
            dest.writeInt(height);
            dest.writeInt((int) type);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PhotoSize> CREATOR = new Creator<PhotoSize>() {
            @Override
            public PhotoSize createFromParcel(Parcel in) {
                return new PhotoSize(in);
            }

            @Override
            public PhotoSize[] newArray(int size) {
                return new PhotoSize[size];
            }
        };

        @Override
        public String toString() {
            return src;
        }

        private String getSrc(JSONObject source) {
            return source.has("src")
                    ? source.optString("src")
                    : source.optString("url");
        }
    }
}