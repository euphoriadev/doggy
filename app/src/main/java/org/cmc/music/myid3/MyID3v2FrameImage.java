/*
 * Written By Charles M. Chen
 *
 * Created on Sep 2, 2005
 *
 */

package org.cmc.music.myid3;

import org.cmc.music.metadata.ImageData;

public class MyID3v2FrameImage extends MyID3v2FrameData {
    public final byte imageData[];
    public final String mimeType;
    public final String description;
    public final int pictureType;

    public MyID3v2FrameImage(String frame_id, byte data_bytes[],
                             final ID3v2FrameFlags flags, final byte imageData[],
                             final String mimeType, final String description,
                             final int pictureType) {
        super(frame_id, data_bytes, flags);
        this.imageData = imageData;
        this.mimeType = mimeType;
        this.description = description;
        this.pictureType = pictureType;
    }

    public ImageData getImageData() {
        return new ImageData(imageData, mimeType, description, pictureType);
    }

}
