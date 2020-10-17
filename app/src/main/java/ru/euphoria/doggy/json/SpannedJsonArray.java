package ru.euphoria.doggy.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;

public class SpannedJsonArray extends JSONArray {
    public SpannedJsonArray(String json) throws JSONException {
        super(new JSONTokener(json));
    }

    public ArrayList<CharSequence> getCharSequences(int indentSpaces) throws JSONException {
        SpannedJsonStringer stringer = new SpannedJsonStringer(indentSpaces);
        writeTo(stringer);
        return stringer.getCharSequences();
    }

    private void writeTo(SpannedJsonStringer stringer) throws JSONException {
        stringer.array();
        for (int i = 0, l = length(); i < l; i++) {
            Object object = opt(i);
            stringer.value(object);
        }
        stringer.endArray();
        stringer.preGetCharSequences();
    }
}