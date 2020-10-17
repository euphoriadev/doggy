package org.cmc.music.myid3;

public class ID3v2FrameFlags implements MyID3v2Constants {
    public static final String TAG_ALTER_PRESERVATION = "Tag Alter Preservation";
    public static final String FILE_ALTER_PRESERVATION = "File Alter Preservation";
    public static final String READ_ONLY = "Read Only";
    public static final String GROUPING_IDENTITY = "Grouping Identity";
    public static final String COMPRESSION = "Compression";
    public static final String ENCRYPTION = "Encryption";
    public static final String UNSYNCHRONISATION = "Unsynchronisation";
    public static final String DATA_LENGTH_INDICATOR = "Data Length Indicator";

    private boolean tagAlterPreservation = false;
    private boolean fileAlterPreservation = false;
    private boolean readOnly = false;
    private boolean groupingIdentity = false;
    private boolean compression = false;
    private boolean encryption = false;
    private boolean unsynchronisation = false;
    private boolean dataLengthIndicator = false;

    public boolean getTagAlterPreservation() {
        return tagAlterPreservation;
    }

    public boolean getFileAlterPreservation() {
        return fileAlterPreservation;
    }

    public boolean getReadOnly() {
        return readOnly;
    }

    public boolean getGroupingIdentity() {
        return groupingIdentity;
    }

    public boolean getCompression() {
        return compression;
    }

    public boolean getEncryption() {
        return encryption;
    }

    public boolean getUnsynchronisation() {
        return unsynchronisation;
    }

    public boolean getDataLengthIndicator() {
        return dataLengthIndicator;
    }

    public void setTagAlterPreservation(boolean value) {
        tagAlterPreservation = value;
    }

    public void setFileAlterPreservation(boolean value) {
        fileAlterPreservation = value;
    }

    public void setReadOnly(boolean value) {
        readOnly = value;
    }

    public void setGroupingIdentity(boolean value) {
        groupingIdentity = value;
    }

    public void setCompression(boolean value) {
        compression = value;
    }

    public void setEncryption(boolean value) {
        encryption = value;
    }

    public void setUnsynchronisation(boolean value) {
        unsynchronisation = value;
    }

    public void setDataLengthIndicator(boolean value) {
        dataLengthIndicator = value;
    }

    public boolean hasSetFlag() {
        return tagAlterPreservation || fileAlterPreservation || readOnly
                || groupingIdentity || compression || encryption
                || unsynchronisation || dataLengthIndicator;
    }

    public ID3v2FrameFlags() {
    }

    public String getSummary() {
        StringBuilder result = new StringBuilder();

        result.append("{");

        if (getTagAlterPreservation())
            result.append("ID3v2FrameFlags: TagAlterPreservation" + ", ");
        if (getFileAlterPreservation())
            result.append("ID3v2FrameFlags: FileAlterPreservation" + ", ");
        if (getReadOnly())
            result.append("ID3v2FrameFlags: ReadOnly" + ", ");
        if (getGroupingIdentity())
            result.append("ID3v2FrameFlags: GroupingIdentity" + ", ");
        if (getCompression())
            result.append("ID3v2FrameFlags: Compression" + ", ");
        if (getEncryption())
            result.append("ID3v2FrameFlags: Encryption" + ", ");
        if (getUnsynchronisation())
            result.append("ID3v2FrameFlags: Unsynchronisation" + ", ");
        if (getDataLengthIndicator())
            result.append("ID3v2FrameFlags: DataLengthIndicator" + ", ");

        result.append("}");

        return result.toString();
    }

}
