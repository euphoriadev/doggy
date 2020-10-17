package ru.euphoria.doggy.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Describes a attachment object from VK.
 */
public class Attachments implements Parcelable {
    public ArrayList<Photo> photos = new ArrayList<>();
    public ArrayList<Audio> audios = new ArrayList<>();
    public ArrayList<Video> videos = new ArrayList<>();
    public ArrayList<Document> docs = new ArrayList<>();
    public ArrayList<Link> links = new ArrayList<>();
    public ArrayList<Sticker> stickers = new ArrayList<>();
    public ArrayList<Gift> gifts = new ArrayList<>();
    public ArrayList<Wall> walls = new ArrayList<>();
    public ArrayList<AudioMessage> voices = new ArrayList<>();
    public ArrayList<Call> calls = new ArrayList<>();

    /** Attachment is a photo. */
    public static final String TYPE_PHOTO = "photo";

    /** Attachment is a video. */
    public static final String TYPE_VIDEO = "video";

    /** Attachment is an audio. */
    public static final String TYPE_AUDIO = "audio";

    /** Attachment is a document. */
    public static final String TYPE_DOC = "doc";

    /** Attachment is a wall post. */
    public static final String TYPE_POST = "wall";

    /** Attachment is a posted photo. */
    public static final String TYPE_POSTED_PHOTO = "posted_photo";

    /** Attachment is a link */
    public static final String TYPE_LINK = "link";

    /** Attachment is a note. */
    public static final String TYPE_NOTE = "note";

    /** Attachment is an application content. */
    public static final String TYPE_APP = "app";

    /** Attachment is a poll. */
    public static final String TYPE_POLL = "poll";

    /** Attachment is a WikiPage. */
    public static final String TYPE_WIKI_PAGE = "page";

    /** Attachment is a PhotoAlbum. */
    public static final String TYPE_ALBUM = "album";

    /** Attachment is a Sticker. */
    public static final String TYPE_STICKER = "sticker";

    /** Attachment is a Gift. */
    public static final String TYPE_GIFT = "gift";

    /** Attachment is a Gift. */
    public static final String TYPE_CALL = "call";

    /** Attachment is a Gift. */
    public static final String TYPE_AUDIO_MESSAGE = "audio_message";

    public Attachments(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            String type = item.optString("type");
            JSONObject object = item.optJSONObject(type);

            switch (type) {
                case TYPE_PHOTO:
                    photos.add(new Photo(object));
                    break;
                case TYPE_AUDIO:
                    audios.add(new Audio(object));
                    break;
                case TYPE_VIDEO:
                    videos.add(new Video(object));
                    break;
                case TYPE_DOC:
                    docs.add(new Document(object));
                    break;
                case TYPE_POST:
                    walls.add(new Wall(object));
                    break;
                case TYPE_LINK:
                    links.add(new Link(object));
                    break;
                case TYPE_STICKER:
                    stickers.add(new Sticker(object));
                    break;
                case TYPE_GIFT:
                    gifts.add(new Gift(object));
                    break;
                case TYPE_AUDIO_MESSAGE:
                    voices.add(new AudioMessage(object));
                    break;
                case TYPE_CALL:
                    calls.add(new Call(object));
                    break;
            }
        }
    }

    protected Attachments(Parcel in) {
        photos = in.createTypedArrayList(Photo.CREATOR);
        audios = in.createTypedArrayList(Audio.CREATOR);
        videos = in.createTypedArrayList(Video.CREATOR);
        docs = in.createTypedArrayList(Document.CREATOR);
        voices = in.createTypedArrayList(AudioMessage.CREATOR);
        calls = in.createTypedArrayList(Call.CREATOR);
    }

    public static final Creator<Attachments> CREATOR = new Creator<Attachments>() {
        @Override
        public Attachments createFromParcel(Parcel in) {
            return new Attachments(in);
        }

        @Override
        public Attachments[] newArray(int size) {
            return new Attachments[size];
        }
    };

    public int size() {
        return getSize(photos)
                + getSize(audios)
                + getSize(videos)
                + getSize(docs)
                + getSize(links)
                + getSize(stickers)
                + getSize(gifts)
                + getSize(walls)
                + getSize(voices)
                + getSize(calls);
    }

    private int getSize(List<?> source) {
        return source != null && !source.isEmpty() ? source.size() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(photos);
        dest.writeTypedList(audios);
        dest.writeTypedList(videos);
        dest.writeTypedList(docs);
        dest.writeTypedList(voices);
        dest.writeTypedList(calls);
    }
}