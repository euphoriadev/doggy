package ru.euphoria.doggy.common;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Single;
import ru.euphoria.doggy.util.AndroidUtil;

public class UpdateChecker {
    private static final String URL = "https://gist.githubusercontent.com/igmorozkin/d7d17a5570e170304caf992af335f6c7/raw/updates.json";

    public static Single<Config> config() {
        return AndroidUtil.request(URL)
                .map(Config::from);
    }

    public static class Config {
        public static final String TYPE_PAGE = "page";
        public static final String TYPE_DIRECT = "direct";

        public String version;
        public String type;
        public String link;
        public String msg;
        public int build;

        public Config(JSONObject source) {
            this.version = source.optString("version");
            this.type = source.optString("type");
            this.link = source.optString("link");
            this.msg = source.optString("msg");
            this.build = source.optInt("build");
        }

        public static Config from(String response) throws JSONException {
            return new Config(new JSONObject(response));
        }
    }
}
