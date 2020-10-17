package ru.euphoria.doggy.yandex;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.Request;
import ru.euphoria.doggy.common.Tokens;
import ru.euphoria.doggy.util.AndroidUtil;

@Deprecated
public class Locator {
    private static final String BASE_URL = "http://api.lbs.yandex.net/geolocation";
    private static final String API_KEY = Tokens.YANDEX_LOCATOR_API_KEY;

    public static Location locate() throws Exception {
        String json = json().toString();

        FormBody.Builder body = new FormBody.Builder();
        body.add("json", json);

        Request request = new Request.Builder()
                .url(BASE_URL)
                .method("POST", body.build())
                .build();
        JSONObject response = new JSONObject(AndroidUtil.requestSync(request));
        JSONObject position = response.optJSONObject("position");

        Location location = new Location("yandex.locator");
        location.setLatitude(position.optDouble("latitude"));
        location.setLongitude(position.optDouble("longitude"));
        location.setAltitude(position.optDouble("altitude"));

        return location;
    }

    private static JSONObject json() throws JSONException {
        JSONObject common = new JSONObject();
        common.put("version", "1.0");
        common.put("api_key", API_KEY);
        common.put("countrycode", Locale.getDefault().getCountry());

        JSONObject json = new JSONObject();
        json.put("common", common);
        return json;
    }
}
