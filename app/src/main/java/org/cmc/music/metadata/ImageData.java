/**
 *
 */
package org.cmc.music.metadata;

public class ImageData {
    public final byte imageData[];
    public final String mimeType;
    public final String description;
    public final int pictureType;

    public ImageData(final byte imageData[], final String mimeType,
                     final String description, final int pictureType) {
        this.imageData = imageData;
        this.mimeType = mimeType;
        this.description = description;
        this.pictureType = pictureType;
    }

}