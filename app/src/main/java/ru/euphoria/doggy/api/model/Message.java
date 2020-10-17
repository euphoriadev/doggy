package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Describes a message object from VK.
 */
@Entity(tableName = "messages", indices = {
        @Index("peer_id"),
        @Index("date")
})
public class Message implements Parcelable {
    public static final int UNREAD = 1;       // message unread
    public static final int OUTBOX = 2;       // исходящее сообщение
    public static final int REPLIED = 4;      // на сообщение был создан ответ
    public static final int IMPORTANT = 8;    // помеченное сообщение
    public static final int CHAT = 16;        // сообщение отправлено через диалог
    public static final int FRIENDS = 32;     // сообщение отправлено другом
    public static final int SPAM = 64;        // сообщение помечено как "Спам"
    public static final int DELETED = 128;    // сообщение удалено (в корзине)
    public static final int FIXED = 256;      // сообщение проверено пользователем на спам
    public static final int MEDIA = 512;      // сообщение содержит медиаконтент
    public static final int BESEDA = 8192;    // беседа
    public static final int DELETE_FOR_ALL = 131072;    // сообщение удалено для всех получателей

    public static final String ACTION_CHAT_CREATE = "chat_create";
    public static final String ACTION_CHAT_INVITE_USER = "chat_invite_user";
    public static final String ACTION_CHAT_KICK_USER = "chat_kick_user";

    public static final String ACTION_CHAT_TITLE_UPDATE = "chat_title_update";
    public static final String ACTION_CHAT_PHOTO_UPDATE = "chat_photo_update";
    public static final String ACTION_CHAT_PHOTO_REMOVE = "chat_photo_remove";

    /** Message ID. (Not returned for forwarded messages), positive number */
    @PrimaryKey
    public int id;

    /** Title of message or chat. */
    @Deprecated
    @Ignore
    public String title;

    /** Chat ID */
    @Deprecated
    @Ignore
    public int chat_id;

    /** Message author ID. */
    public int from_id;

    /** Message peer ID. */
    public int peer_id;

    /** Date (in Unix time) when the message was sent. */
    public long date;

    /** Message text */
    public String text;

    /** Message status (false — not read, true — read). (Not returned for forwarded messages */
    public boolean read_state;

    /** Type (false — received, true — sent). (Not returned for forwarded messages.) */
    public boolean is_out;

    /** User IDs of chat participants */
    @Ignore
    public int[] chat_members;

    /** ID of user who started the chat. */
    @Ignore
    public long admin_id;

    /** Number of chat participants. */
    @Ignore
    public int users_count;

    /** Whether the message is deleted (false — no, true — yes). */
    public boolean is_deleted;

    /** Whether the message is important */
    public boolean important;

    /** Whether the message contains smiles (false — no, true — yes). */
    public boolean emoji;

    /** Array of forwarded messages (if any). */
    @Ignore
    public ArrayList<Message> fwd_messages;

    /** URL of chat image with width size of 50px */
    public String photo_50;

    /** URL of chat image with width size of 100px */
    @Ignore
    public String photo_100;

    /** URL of chat image with width size of 200px */
    @Ignore
    public String photo_200;

    /** The count of unread messages. */
    @Ignore
    public int unread;

    /** Field transferred, if a service message. */
    @Ignore
    public String action;

    /** The chat name, for service messages. */
    @Ignore
    public String action_text;

    /** User ID (if > 0) or email (if < 0), who was invited or kicked. */
    @Ignore
    public int action_mid;

    @Ignore
    public Attachments attachments;

    @Ignore
    public static int count;

    /** Creates a new empty message instance */
    public Message() {
    }

    /**
     * Creates a new message model with fields from json source.
     *
     * @param source the json source to parse
     */
    public Message(JSONObject source) {
        this.id = source.optInt("id");
        this.from_id = source.optInt("from_id");
        this.chat_id = source.optInt("chat_id");
        this.peer_id = source.optInt("peer_id");
        this.date = source.optLong("date");
        this.is_out = source.optLong("out") == 1;
        this.read_state = source.optLong("read_state") == 1;
        this.unread = source.optInt("unread");
        this.title = source.optString("title");
        this.text = source.optString("text");
        this.admin_id = source.optInt("admin_id");
        this.users_count = source.optInt("users_count");
        this.is_deleted = source.optInt("deleted") == 1;
        this.important = source.optInt("important") == 1;
        this.emoji = source.optLong("emoji") == 1;
        this.action = source.optString("action");
        this.action_text = source.optString("action_text");
        this.action_mid = source.optInt("action_mid");
        this.photo_50 = source.optString("photo_50");
        this.photo_100 = source.optString("photo_100");
        this.photo_200 = source.optString("photo_200");

        JSONArray active = source.optJSONArray("chat_active");
        if (active != null) {
            this.chat_members = new int[active.length()];
            for (int i = 0; i < active.length(); i++) {
                chat_members[i] = active.optInt(i);
            }
        }

        JSONArray messages = source.optJSONArray("fwd_messages");
        if (messages != null) {
            fwd_messages = new ArrayList<>(messages.length());
            for (int i = 0; i < messages.length(); i++) {
                fwd_messages.add(new Message(messages.optJSONObject(i)));
            }
        }

        JSONArray attachs = source.optJSONArray("attachments");
        if (attachs != null) {
            attachments = new Attachments(attachs);
        }
    }

    protected Message(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.chat_id = in.readInt();
        this.from_id = in.readInt();
        this.peer_id = in.readInt();
        this.date = in.readLong();
        this.text = in.readString();
        this.read_state = in.readByte() != 0;
        this.is_out = in.readByte() != 0;
        this.chat_members = in.createIntArray();
        this.admin_id = in.readLong();
        this.users_count = in.readInt();
        this.is_deleted = in.readByte() != 0;
        this.important = in.readByte() != 0;
        this.emoji = in.readByte() != 0;
        this.fwd_messages = in.createTypedArrayList(Message.CREATOR);
        this.photo_50 = in.readString();
        this.photo_100 = in.readString();
        this.photo_200 = in.readString();
        this.unread = in.readInt();
        this.action = in.readString();
        this.action_text = in.readString();
        this.action_mid = in.readInt();
        this.attachments = in.readParcelable(Attachments.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeInt(chat_id);
        dest.writeInt(from_id);
        dest.writeInt(peer_id);
        dest.writeLong(date);
        dest.writeString(text);
        dest.writeByte((byte) (read_state ? 1 : 0));
        dest.writeByte((byte) (is_out ? 1 : 0));
        dest.writeIntArray(chat_members);
        dest.writeLong(admin_id);
        dest.writeInt(users_count);
        dest.writeByte((byte) (is_deleted ? 1 : 0));
        dest.writeByte((byte) (important ? 1 : 0));
        dest.writeByte((byte) (emoji ? 1 : 0));
        dest.writeTypedList(fwd_messages);
        dest.writeString(photo_50);
        dest.writeString(photo_100);
        dest.writeString(photo_200);
        dest.writeInt(unread);
        dest.writeString(action);
        dest.writeString(action_text);
        dest.writeInt(action_mid);
        dest.writeParcelable(attachments, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    /**
     * Returns true if this message are deleted.
     *
     * @param flags the message flags
     */
    public static boolean isDeleted(int flags) {
        return (flags & DELETED) != 0
                || (flags & DELETE_FOR_ALL) != 0;
    }

    /**
     * Returns true if this message are important.
     *
     * @param flags the message flags
     */
    public static boolean isImportant(int flags) {
        return (flags & IMPORTANT) != 0;
    }

    /**
     * Returns true if this message are unread.
     *
     * @param flags the message flags
     */
    public static boolean isUnread(int flags) {
        return (flags & UNREAD) != 0;
    }


    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("peer_id", peer_id);
        json.put("from_id", from_id);
        json.put("date", date);
        json.put("is_out", is_out);
        json.put("read_state", read_state);
        json.put("unread", unread);
        json.put("text", text);
        json.put("important", important);
        json.put("emoji", emoji);

        return json;
    }

    @Override
    public String toString() {
        return text;
    }
}
