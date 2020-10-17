package ru.euphoria.doggy.util;

import android.media.MediaMetadataRetriever;

import java.util.HashMap;

import io.reactivex.Single;

public class VideoUtil {

    public static Single<Integer> getVideoBitrate(String url) {
        return Single.create(emitter -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(url, new HashMap<>());
                String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                emitter.onSuccess(Integer.parseInt(bitrate));
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                retriever.release();
            }
        });
    }
}
