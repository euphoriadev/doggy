package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.json.JSONObject;

import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.db.converter.IntArrayTypeConverter;

import static ru.euphoria.doggy.api.model.Attachments.TYPE_AUDIO_MESSAGE;

/**
 * Describes a audio message object from VK
 */
@Entity(tableName = "voices")
public class AudioMessage extends Attachment implements Parcelable {
    /** Audio Message ID. */
    @PrimaryKey
    public int id;

    /** Audio owner ID. */
    public int owner_id;

    /** Duration (in seconds). */
    public int duration;

    /** The array of waveform */
    @TypeConverters(IntArrayTypeConverter.class)
    public int[] waveform;

    /** Link to ogg. */
    public String link_ogg;

    /** Link to mp3. */
    public String link_mp3;

    /** Text of voice, testing feature */
    public String transcript;

    public String transcript_state;

    /** Access key */
    public String access_key;

    public AudioMessage() {

    }

    /**
     * Creates a new message model with fields from json source.
     *
     * @param source the json source to parse
     */
    public AudioMessage(JSONObject source) {
        this.id = source.optInt("id");
        this.owner_id = source.optInt("owner_id");
        this.duration = source.optInt("duration");
        this.link_ogg = source.optString("link_ogg");
        this.link_mp3 = source.optString("link_mp3");
        this.access_key = source.optString("access_key");
        this.transcript = source.optString("transcript");
        this.transcript_state = source.optString("transcript_state");

        this.waveform = VKApi.parseArray(source.optJSONArray("waveform"));
    }

    protected AudioMessage(Parcel in) {
        super(in);
        id = in.readInt();
        owner_id = in.readInt();
        duration = in.readInt();
        waveform = in.createIntArray();
        link_ogg = in.readString();
        link_mp3 = in.readString();
        transcript = in.readString();
        transcript_state = in.readString();
        access_key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeInt(duration);
        dest.writeIntArray(waveform);
        dest.writeString(link_ogg);
        dest.writeString(link_mp3);
        dest.writeString(transcript);
        dest.writeString(transcript_state);
        dest.writeString(access_key);
    }

    @Override
    public String toAttachmentString() {
        StringBuilder result = new StringBuilder(TYPE_AUDIO_MESSAGE)
                .append(owner_id)
                .append('_')
                .append(id);
        if (!TextUtils.isEmpty(access_key)) {
            result.append('_');
            result.append(access_key);
        }
        return result.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AudioMessage> CREATOR = new Creator<AudioMessage>() {
        @Override
        public AudioMessage createFromParcel(Parcel in) {
            return new AudioMessage(in);
        }

        @Override
        public AudioMessage[] newArray(int size) {
            return new AudioMessage[size];
        }
    };
}
