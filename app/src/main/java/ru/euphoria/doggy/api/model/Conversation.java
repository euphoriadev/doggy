package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import ru.euphoria.doggy.api.VKApi;

/**
 * Object describes dialog with user or community
 * or group chat and contains following fields..
 */
public class Conversation implements Parcelable {
    /** ID of the last read incoming message. */
    public int in_read;

    /** ID of the last read outcoming message. */
    public int out_read;

    /** Number of unread messages */
    public int unread_count;

    /** true, if the conversation marked as important (only for community messages). */
    public boolean important;

    /** true, if the conversation marked as unanswered (only for community messages). */
    public boolean unanswered;

    /** information about location. */
    public Peer peer;

    /** Conversation settings. */
    public ChatSettings chat_settings;

    public Conversation(JSONObject source) {
        this.in_read = source.optInt("in_read");
        this.out_read = source.optInt("out_read");
        this.unread_count = source.optInt("unread_count");
        this.important = source.optInt("important") == 1;
        this.unanswered = source.optInt("unanswered") == 1;
        this.peer = new Peer(source.optJSONObject("peer"));
        this.chat_settings = new ChatSettings(source.optJSONObject("chat_settings"));
    }

    public Conversation() {
    }

    public static class Peer implements Parcelable {
        public static final String TYPE_USER = "user";
        public static final String TYPE_CHAT = "chat";
        public static final String TYPE_GROUP = "group";

        /** Destination ID. */
        public int id;

        /**
         * local destination ID.
         * For conversations — id - 2000000000,
         * for community — -id,
         * for e-mail — -(id+2000000000).
         */
        public int local_id;

        /** Conversation type. possible values: user, chat, group, email. */
        public String type;

        public Peer(JSONObject source) {
            if (source == null) return;
            this.id = source.optInt("id");
            this.local_id = source.optInt("local_id");
            this.type = source.optString("type");
        }

        protected Peer(Parcel in) {
            id = in.readInt();
            local_id = in.readInt();
            type = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(local_id);
            dest.writeString(type);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Peer> CREATOR = new Creator<Peer>() {
            @Override
            public Peer createFromParcel(Parcel in) {
                return new Peer(in);
            }

            @Override
            public Peer[] newArray(int size) {
                return new Peer[size];
            }
        };
    }

    public static class ChatSettings implements Parcelable {
        /** Conversation members number. */
        public int members_count;

        /** Conversation title. */
        public String title;
        /**
         * Current user state; possible values:
         * in — consist in the conversation,
         * kicked — kicked out from conversation,
         * left — left the conversation.
         */
        public String state;

        /** Conversation cover image */
        public Photo photo;

        /** IDs of the last users who wrote to the conversation */
        public int[] active_ids;

        public ChatSettings(JSONObject source) {
            if (source == null) return;
            this.members_count = source.optInt("members_count");
            this.title = source.optString("title");
            this.state = source.optString("state");
            this.photo = new Photo(source.optJSONObject("photo"));
            this.active_ids = VKApi.parseArray(source.optJSONArray("active_ids"));
        }

        public static class Photo implements Parcelable {
            /** URL of conversation image with width size of 50 px. */
            public String photo_50;

            /** URL of conversation image with width size of 100 px. */
            public String photo_100;

            /** URL of conversation image with width size of 200 px. */
            public String photo_200;

            public Photo(JSONObject source) {
                if (source == null) return;

                this.photo_50 = source.optString("photo_50");
                this.photo_100 = source.optString("photo_100");
                this.photo_200 = source.optString("photo_200");
            }

            protected Photo(Parcel in) {
                photo_50 = in.readString();
                photo_100 = in.readString();
                photo_200 = in.readString();
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(photo_50);
                dest.writeString(photo_100);
                dest.writeString(photo_200);
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
        }

        protected ChatSettings(Parcel in) {
            members_count = in.readInt();
            title = in.readString();
            state = in.readString();
            photo = in.readParcelable(Photo.class.getClassLoader());
            active_ids = in.createIntArray();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(members_count);
            dest.writeString(title);
            dest.writeString(state);
            dest.writeParcelable(photo, flags);
            dest.writeIntArray(active_ids);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ChatSettings> CREATOR = new Creator<ChatSettings>() {
            @Override
            public ChatSettings createFromParcel(Parcel in) {
                return new ChatSettings(in);
            }

            @Override
            public ChatSettings[] newArray(int size) {
                return new ChatSettings[size];
            }
        };
    }

    protected Conversation(Parcel in) {
        in_read = in.readInt();
        out_read = in.readInt();
        unread_count = in.readInt();
        important = in.readByte() != 0;
        unanswered = in.readByte() != 0;
        peer = in.readParcelable(Peer.class.getClassLoader());
        chat_settings = in.readParcelable(ChatSettings.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(in_read);
        dest.writeInt(out_read);
        dest.writeInt(unread_count);
        dest.writeByte((byte) (important ? 1 : 0));
        dest.writeByte((byte) (unanswered ? 1 : 0));
        dest.writeParcelable(peer, flags);
        dest.writeParcelable(chat_settings, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}
