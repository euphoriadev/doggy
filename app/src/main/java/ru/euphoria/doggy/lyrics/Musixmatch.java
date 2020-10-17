package ru.euphoria.doggy.lyrics;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Request;
import ru.euphoria.doggy.util.AndroidUtil;

public class Musixmatch {
    public static final String USER_AGENT = AndroidUtil.MOBILE_USER_AGENT;

    public static String lyrics(String artist, String song, boolean forceSearch) throws IOException {
        artist = pretty(artist);
        song = pretty(song);

        System.out.println(artist + " - " + song);
        String url = String.format(Locale.ROOT,
                "https://www.musixmatch.com/lyrics/%s/%s", artist, song);

        String html = lyrics(url);
        if (!TextUtils.isEmpty(html)) {
            return html;
        }
        if (forceSearch) {
            String link = link(artist + " " + song);
            if (!TextUtils.isEmpty(link)) {
                return lyrics("https://www.musixmatch.com" + link);
            }
        }
        return html;
    }

    public static String lyrics(String url) throws IOException {
        Document document = parse(url);
        Elements content = document.getElementsByClass("lyrics__content__ok");
        if (content.isEmpty()) {
            content = document.getElementsByClass("lyrics__content__warning");
        }

        return content.html();
    }

    public static String link(String q) throws IOException {
        Document document = parse("https://www.musixmatch.com/search/" + q);
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String href = link.attr("href");
            if (href.startsWith("/lyrics")) {
                return href;
            }
        }
        return "";
    }

    private static String pretty(String name) {
        return TextUtils.join("-", name.split("\\W+"));
    }

    private static Document parse(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();
        String html = AndroidUtil.requestSync(request);
        Document document = Jsoup.parse(html, url);
        document.outputSettings().prettyPrint(false);

        return document;
    }
}
