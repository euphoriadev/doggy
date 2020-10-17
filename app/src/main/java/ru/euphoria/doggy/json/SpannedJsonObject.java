package ru.euphoria.doggy.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class SpannedJsonObject extends JSONObject {
    public SpannedJsonObject(String json) throws JSONException {
        super(json);
    }

    public ArrayList<CharSequence> getCharSequences(int indentSpaces) throws JSONException {
        SpannedJsonStringer stringer = new SpannedJsonStringer(indentSpaces);
        writeTo(stringer);
        return stringer.getCharSequences();
    }

    private void writeTo(SpannedJsonStringer stringer) throws JSONException {
        stringer.object();
        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            String key = keys.next();
            stringer.key(key).value(opt(key));
        }
        stringer.endObject();
        stringer.preGetCharSequences();

    }
}
