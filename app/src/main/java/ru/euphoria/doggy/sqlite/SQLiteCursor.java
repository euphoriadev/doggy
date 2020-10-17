package ru.euphoria.doggy.sqlite;

public class SQLiteCursor {
    public static final int FIELD_TYPE_INT = 1;
    public static final int FIELD_TYPE_FLOAT = 2;
    public static final int FIELD_TYPE_STRING = 3;
    public static final int FIELD_TYPE_BYTEARRAY = 4;
    public static final int FIELD_TYPE_NULL = 5;

    public static native String fastString();

    private native int columnType(long statementHandle, int columnIndex);
    private native int columnCount(long statementHandle);
    private native int columnIsNull(long statementHandle, int columnIndex);
    private native int columnIntValue(long statementHandle, int columnIndex);
    private native long columnLongValue(long statementHandle, int columnIndex);
    private native double columnDoubleValue(long statementHandle, int columnIndex);
    private native String columnStringValue(long statementHandle, int columnIndex);
    private native long columnByteBufferValue(long statementHandle, int columnIndex);
}
