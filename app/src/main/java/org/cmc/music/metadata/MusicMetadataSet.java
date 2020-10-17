package org.cmc.music.metadata;

import org.cmc.music.fs.FSParser;
import org.cmc.music.myid3.ID3Tag;
import org.cmc.music.myid3.TagFormat;

import java.util.Map;
import java.util.Vector;

public class MusicMetadataSet {
    public final ID3Tag id3v1Raw;
    public final ID3Tag.V2 id3v2Raw;
    public final MusicMetadata id3v1Clean;
    public final MusicMetadata id3v2Clean;
    public final MusicMetadata filename;
    public final MusicMetadata merged;

    private MusicMetadataSet(ID3Tag id3_v1_raw, ID3Tag.V2 id3_v2_raw,
                             MusicMetadata id3_v1_clean, MusicMetadata id3_v2_clean,
                             String file_name, String folder_name) {
        this.id3v1Raw = id3_v1_raw;
        this.id3v2Raw = id3_v2_raw;
        this.id3v1Clean = id3_v1_clean;
        this.id3v2Clean = id3_v2_clean;
        this.filename = FSParser.parseFilename(file_name, folder_name);
        this.merged = new MusicMetadata("merged");

        merge();
    }

    public IMusicMetadata getSimplified() {
        return new MusicMetadata(merged);
    }

    public static final String newline = System.getProperty("line.separator");

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{ID3TagSet. ");

        result.append(newline);
        result.append("v1_raw: ").append(id3v1Raw);
        result.append(newline);
        result.append("v2_raw: ").append(id3v2Raw);
        result.append(newline);
        result.append("v1: ").append(id3v1Clean);
        result.append(newline);
        result.append("v2: ").append(id3v2Clean);
        result.append(newline);
        result.append("filename: ").append(filename);
        result.append(newline);
        result.append("merged: ").append(merged);
        result.append(newline);

        result.append(" }");

        return result.toString();
    }

    private final void merge(Map src) {
        if (src == null)
            return;

        Vector keys = new Vector(src.keySet());
        for (int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            if (null != merged.get(key))
                continue;
            Object value = src.get(key);
            merged.put(key, value);
        }
    }

    private final void merge() {
        if (id3v2Clean != null)
            merged.putAll(id3v2Clean);

        merge(id3v1Clean);
        merge(filename);
    }

    private static final TagFormat utils = new TagFormat();

    public static final MusicMetadataSet factoryMethod(ID3Tag id3_v1_raw,
                                                       ID3Tag.V2 id3_v2_raw, String filename, String folder_name) {
        MusicMetadata id3_v1_clean = id3_v1_raw == null ? null : utils
                .process(id3_v1_raw.values);
        MusicMetadata id3_v2_clean = id3_v2_raw == null ? null : utils
                .process(id3_v2_raw.values);

        return new MusicMetadataSet(id3_v1_raw, id3_v2_raw, id3_v1_clean,
                id3_v2_clean, filename, folder_name);
    }

}
