package ru.euphoria.doggy.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Response<E> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected JSONObject source;
    protected JSONArray items;
    private int count;

    public Response(JSONObject source) {
        this.source = source;
        this.count = VKApi.optCount(source);
        this.items = VKApi.optJsonArray(source);
    }

    public JSONObject source() {
        return source;
    }

    public ArrayList<E> items(Class<E> cls) {
        return VKApi.from(cls, items);
    }

    public int count() {
        return count;
    }
}
