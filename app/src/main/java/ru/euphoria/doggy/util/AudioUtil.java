package ru.euphoria.doggy.util;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.text.TextUtils;

import androidx.core.util.Consumer;

import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.method.ParamSetter;
import ru.euphoria.doggy.api.model.Album;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.itunes.SearchResult;
import ru.euphoria.doggy.lyrics.Musixmatch;

public class AudioUtil {
    private static Cache cache;

    public static synchronized Cache getCache() {
        if (cache == null) {
            File cacheDir = new File(AppContext.context.getCacheDir(), "audio-cache");
            cache = new SimpleCache(cacheDir, new NoOpCacheEvictor(),
                    AppContext.databaseProvider);
        }
        return cache;
    }

    public static MediaDescriptionCompat getMediaDescriptor(Audio audio) {
        Bundle extras = new Bundle();

//        Bitmap album = ImageUtil.getBitmap(audio.coverMedium());
//        if (album != null) {
//            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, album);
//            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, album);
//        }

        return new MediaDescriptionCompat.Builder()
//                .setIconBitmap(album)
                .setMediaId(String.valueOf(audio.id))
                .setTitle(audio.title)
                .setDescription(audio.artist)
                .setExtras(extras)
                .build();
    }

    public static String getReceipt() {
        return "d4gdb0joSiM:APA91bFAM-gVwLCkCABy5DJPPRH5TNDHW9xcGu_OLhmdUSA8zuUsBiU_DexHrTLLZWtzWHZTT5QUaVkBk_GJVQyCE_yQj9UId3pU3vxvizffCPQISmh2k93Fs7XH1qPbDvezEiMyeuLDXb5ebOVGehtbdk_9u5pwUw";
    }

    private static ParamSetter fix(ParamSetter setter) {
        return setter
                .accessToken(SettingsStore.getAudioAccessToken())
                .v(AndroidUtil.getKateVersionApi())
                .userAgent(AndroidUtil.getKateUserAgent());
    }

    public static Single<List<Audio>> getAudios() {
        return Single.fromCallable(() -> {
            ParamSetter setter = VKApi.audios()
                    .get()
                    .count(5000);

            return VKApi.from(Audio.class, fix(setter).json());
        });
    }

    public static Single<Boolean> remove(Audio audio) {
        return Single.fromCallable(() -> {
            ParamSetter setter = VKApi.audios().delete()
                    .audioId(audio.id)
                    .ownerId(audio.owner_id);

            return fix(setter).json().optInt("response") == 1;
        });
    }

    public static Single<Integer> editLyrics(Audio audio, String lyrics) {
        return Single.fromCallable(() -> {
            ParamSetter setter = VKApi.audios().edit()
                    .audioId(audio.id)
                    .ownerId(audio.owner_id)
                    .title(audio.title)
                    .artist(audio.artist)
                    .genreId(audio.genre)
                    .text(lyrics);

            return fix(setter).json().optInt("response");
        });
    }

    public static Single<String> getLyrics(Audio audio) {
        return Single.fromCallable(() -> {
            JSONObject json = fix(VKApi.audios().getLyrics(audio.lyrics_id)).json();
            return json.optJSONObject("response").optString("text", "");
        }).subscribeOn(Schedulers.io());
    }

    public static Single<String> findLyrics(Audio audio) {
        return Single.fromCallable(() -> {
            String lyrics = getAzLyrics(audio);
            return TextUtils.isEmpty(lyrics)
                    ? getMusixLyrics(audio) : lyrics;
        });
    }

    public static long getSize(int duration, int bitrate) {
        return bitrate * duration / 8;
    }

    public static Single<Integer> getBitrate(Audio audio) {
        return Single.create(emitter -> {
            MediaExtractor extractor = null;
            try {
                extractor = new MediaExtractor();
                extractor.setDataSource(audio.url);

                MediaFormat format = extractor.getTrackFormat(0);
                int bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
                emitter.onSuccess(bitrate);
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (extractor != null) {
                    extractor.release();
                }
            }
        });
    }

    public static Album getAlbum(Audio audio) {
        Album album = AppDatabase.database().albums().get(audio.id);
        if (album == null) {
            album = new Album();
        }
        return album;
    }

    public static String getCover(Audio audio) {
        Album album = getAlbum(audio);
        return album.img;
    }

    public static String gteMediumCover(Audio audio) {
        Album album = getAlbum(audio);
        return album.img_medium;
    }

    public static void searchAlbum(Audio audio, Consumer<Boolean> callback) {
        Call<SearchResult> call = AppContext.itunes.searchAlbum(audio.artist + " " + audio.title);
        call.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, retrofit2.Response<SearchResult> response) {
                SearchResult body = response.body();
                if (body == null) return;

                DebugLog.d("AudioUtil",
                        "onResponse: " + audio.toString() + ", album: " + body.artworkUrl100);

                // У нас есть artworkUrl100, размером 100х100,
                // но это слишком шакально, поэтому будем использовать
                // хотя бы 200x200 в адаптере
                AppDatabase.database().albums().insert(new Album(
                        audio.id,
                        body.buildAlbumUrl(SearchResult.ALBUM_SIZE_SMALL),
                        body.buildAlbumUrl(SearchResult.ALBUM_SIZE_MEDIUM),
                        body.buildMaxAlbumUrl(
                        )));
                callback.accept(true);
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                callback.accept(false);
            }
        });
    }

    private static String getMusixLyrics(Audio audio) throws IOException {
        return getMusixLyrics(audio.artist, audio.title);
    }

    private static String getMusixLyrics(String artist, String title) throws IOException {
        return Musixmatch.lyrics(artist, title, true);
    }

    private static String getBody(HttpUrl url) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("User-Agent", AndroidUtil.MOBILE_USER_AGENT);
        try {
            Response response = AppContext.httpClient
                    .newCall(builder.build())
                    .execute();
            return response.body().string();
        } catch (IOException e) {
            // when flood control
//            e.printStackTrace();
        }
        return "";
    }

    private static String getAzLyrics(Audio audio) throws IOException {
        String artist = AndroidUtil.sub(audio.artist).toLowerCase();
        String title = AndroidUtil.sub(audio.title).toLowerCase();
        if (artist.startsWith("the")) {
            artist = artist.substring(3);
        }

        HttpUrl url = HttpUrl.get(String.format(Locale.getDefault(),
                "https://www.azlyrics.com/lyrics/%s/%s.html", artist, title));
        return parseHtmlLyrics(getBody(url));
    }

    private static String parseMusixmatchLyrics(String body) {
        System.out.println(body);

        String warning = find(body, "class=\"lyrics__content__warning\">(.*?)</span>");
        String ok = find(body, "class=\"lyrics__content__ok\">(.*?)</span>");
        return ok.length() > warning.length() ? ok : warning;
    }

    private static String find(String body, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher matcher = p.matcher(body);

        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            buffer.append(matcher.group(1));
            buffer.append('\n');
        }
        return buffer.toString();
    }

    private static String parseHtmlLyrics(String html) {
        int start = html.indexOf("Sorry about that. -->");
        if (start <= 0) {
            return "";
        }
        int end = html.indexOf("<!-- MxM banner -->");

        return html.substring(start, end)
                .replace("Sorry about that. -->", "")
                .replace("<br>", "")
                .replace("</br>", "")
                .replace("</div>", "")
                .trim();
    }

}
