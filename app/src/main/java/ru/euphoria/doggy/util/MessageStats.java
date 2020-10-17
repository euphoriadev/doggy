package ru.euphoria.doggy.util;

import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.BuildConfig;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.api.model.AudioMessage;
import ru.euphoria.doggy.api.model.Call;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.common.DebugLog;
import ru.euphoria.doggy.common.HashMultiset;
import ru.euphoria.doggy.common.Stopwatch;
import ru.euphoria.doggy.data.SettingsStore;

import static ru.euphoria.doggy.data.SettingsStore.KEY_MSG_IGNORE_LIST;

/**
 * Created by admin on 21.04.18.
 */

@Entity(tableName = "messages_stats")
public class MessageStats {
    private static final String LOG_TAG = "MessageStats";

    private static final List<String> DEFAULT_IGNORE_LIST = Arrays.asList(
            "http", "https", "ru", "com",
            "и", "в", "во", "не", "что", "он", "на", "я", "с", "со", "как", "а", "то", "все",
            "она", "так", "его", "но", "да", "ты", "к", "у", "же", "вы", "за", "бы", "по",
            "только", "ее", "мне", "было", "вот", "от", "меня", "еще", "нет", "о", "из",
            "ему", "теперь", "когда", "даже", "ну", "вдруг", "ли", "если", "уже", "или", "ни",
            "быть", "был", "него", "до", "вас", "нибудь", "опять", "уж", "вам", "ведь", "там",
            "потом", "себя", "ничего", "ей", "может", "они", "тут", "где", "есть", "надо", "ней",
            "для", "мы", "тебя", "их", "чем", "была", "сам", "чтоб", "без", "будто", "чего",
            "раз", "тоже", "себе", "под", "будет", "ж", "тогда", "кто", "это", "этот", "того", "потому",
            "этого", "какой", "совсем", "ним", "здесь", "этом", "один", "почти", "мой", "тем",
            "чтобы", "нее", "сейчас", "были", "куда", "зачем", "всех", "никогда", "можно",
            "при", "наконец", "два", "об", "другой", "хоть", "после", "над", "больше", "тот",
            "через", "эти", "нас", "про", "всего", "них", "какая", "много", "разве", "три",
            "эту", "моя", "впрочем", "хорошо", "свою", "этой", "перед", "иногда", "лучше",
            "чуть", "том", "нельзя", "такой", "им", "более", "всегда", "конечно", "всю", "между",
            "ее", "её"
    );
    private static final ThreadLocal<Pattern> whitespace = new ThreadLocal<Pattern>() {
        @Override
        protected Pattern initialValue() {
            return Pattern.compile("\\w+");
        }
    };
    private static final ThreadLocal<Matcher> whitespaceMatcher = new ThreadLocal<Matcher>() {
        @Override
        protected Matcher initialValue() {
            return Pattern.compile("\\w+").matcher("");
        }
    };
    private static final ThreadLocal<Matcher> emojiMatcher = new ThreadLocal<Matcher>() {
        @Override
        protected Matcher initialValue() {
            Pattern pattern = Pattern.compile("(?:[\\u2700-\\u27bf]|" +
                            "(?:[\\ud83c\\udde6-\\ud83c\\uddff]){2}|" +
                            "[\\ud800\\udc00-\\uDBFF\\uDFFF]|[\\u2600-\\u26FF])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|[\\ud83c\\udffb-\\ud83c\\udfff])?" +
                            "(?:\\u200d(?:[^\\ud800-\\udfff]|" +
                            "(?:[\\ud83c\\udde6-\\ud83c\\uddff]){2}|" +
                            "[\\ud800\\udc00-\\uDBFF\\uDFFF]|[\\u2600-\\u26FF])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|[\\ud83c\\udffb-\\ud83c\\udfff])?)*|" +
                            "[\\u0023-\\u0039]\\ufe0f?\\u20e3|\\u3299|\\u3297|\\u303d|\\u3030|\\u24c2|[\\ud83c\\udd70-\\ud83c\\udd71]|[\\ud83c\\udd7e-\\ud83c\\udd7f]|\\ud83c\\udd8e|[\\ud83c\\udd91-\\ud83c\\udd9a]|[\\ud83c\\udde6-\\ud83c\\uddff]|[\\ud83c\\ude01-\\ud83c\\ude02]|\\ud83c\\ude1a|\\ud83c\\ude2f|[\\ud83c\\ude32-\\ud83c\\ude3a]|[\\ud83c\\ude50-\\ud83c\\ude51]|\\u203c|\\u2049|[\\u25aa-\\u25ab]|\\u25b6|\\u25c0|[\\u25fb-\\u25fe]|\\u00a9|\\u00ae|\\u2122|\\u2139|\\ud83c\\udc04|[\\u2600-\\u26FF]|\\u2b05|\\u2b06|\\u2b07|\\u2b1b|\\u2b1c|\\u2b50|\\u2b55|\\u231a|\\u231b|\\u2328|\\u23cf|[\\u23e9-\\u23f3]|[\\u23f8-\\u23fa]|\\ud83c\\udccf|\\u2934|\\u2935|[\\u2190-\\u21ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
            return pattern.matcher("");
        }
    };

    private static final ThreadLocal<Pattern> explicit = new ThreadLocal<Pattern>() {
        @Override
        protected Pattern initialValue() {
            return Pattern.compile(ArrayUtil.join(
                    new String[]{
                            "сука", "\\Aе?бля+[тд]?", "(ни)?ху[йея]", "пидо?р", "д[оа]лб[ао][её]б",
                            "у[её]бок", "еб[аи][слтнш]", "п[иеё]зд",
                    }, "|"), Pattern.UNICODE_CASE);
        }
    };
    private static final HashSet<String> ignoreExplicit = loadIgnoreExplicit();
    private static final HashSet<String> ignore = loadIgnoreList();

    @Ignore
    private HashMultiset<String> words = new HashMultiset<>();
    @Ignore
    private HashMultiset<String> smiles = new HashMultiset<>();
    @Ignore
    private HashMultiset<Integer> members = new HashMultiset<>();
    @Ignore
    private HashMultiset<Long> days = new HashMultiset<>();

    public static final String TYPE_WORDS = "words";
    public static final String TYPE_MEMBERS = "members";
    public static final String TYPE_SMILES = "smiles";
    public static final String TYPE_DAYS = "days";

    @PrimaryKey
    public int peer;
    public volatile int total;
    public volatile int out;
    public volatile int in;
    public volatile int countWords;
    public volatile int countChars;
    public volatile int forwards;
    public volatile long firstTime = Long.MAX_VALUE;
    public volatile long lastTime = 0;


    public volatile int photos, audios, videos,
            docs, voices, walls, stickers, gifts,
            links, calls, attachments, fucks;

    public volatile long docsSize;
    public volatile int callsDuration, voicesDuration, audiosDuration;
    public volatile int geoPoints;

    public volatile int processed;

    @Ignore
    public MessageStats(int peer) {
        this(peer, 0);
    }

    public MessageStats(int peer, int total) {
        this.peer = peer;
        this.total = total;

        DebugLog.w(LOG_TAG, "regex for explicit words: " + explicit.get().pattern());
    }

    public static Pattern explicitPattern() {
        return explicit.get();
    }

    public HashMultiset<String> words() {
        return words;
    }

    public HashMultiset<String> smiles() {
        return smiles;
    }

    public HashMultiset<Integer> members() {
        return members;
    }

    public HashMultiset<Long> days() {
        return days;
    }

    public void addWords(Message msg, boolean increaseCounters) {
        if (!TextUtils.isEmpty(msg.text)) {
            String body = msg.text.toLowerCase();
            if (increaseCounters) {
                countChars += body.length();
            }

            Matcher matcher = whitespaceMatcher.get().reset(body);
            while (matcher.find()) {
                String token = matcher.group();
                if (token.length() > 2 && !ignore.contains(token)) {
                    words.add(token);
                }
                if (increaseCounters) {
                    countWords++;
                    if (!ignoreExplicit.contains(token)
                            && explicit.get().matcher(token).find()) {
                        fucks++;
                    }

                }
            }

            addSmiles(body);

        }
        if (msg.peer_id > VKApi.PEER_OFFSET) {
            members.add(msg.from_id);
        }
        days.add(TimeUnit.MILLISECONDS.toDays(msg.date * 1000L));
    }

    public void addWords(List<Message> messages, boolean increaseCounters) {
        for (Message msg : messages) {
            addWords(msg, increaseCounters);
        }
    }

    public void add(List<Message> messages, boolean increaseCounters) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (Message msg : messages) {
            if (msg.is_out) {
                out++;
            } else {
                in++;
            }
            firstTime = Math.min(firstTime, msg.date);
            lastTime = Math.max(lastTime, msg.date);

            countAttachments(msg);
            addWords(msg, increaseCounters);
        }
        processed += messages.size();

        if (BuildConfig.DEBUG) {
            String elapsed = stopwatch.toString();
            System.out.println("messages: " + messages.size() + ", words: " + words.size()
                    + " elapsed: " + elapsed);
        }
    }

    private void addSmiles(String body) {
        Matcher matcher = emojiMatcher.get().reset(body);
        while (matcher.find()) {
            String token = matcher.group();
            smiles.add(token);
        }
    }

    public void clear() {
        words.clear();
        days.clear();
        smiles.clear();
        members.clear();
    }

    private synchronized void countAttachments(Message msg) {
        if (msg.attachments != null) {
            this.photos += msg.attachments.photos.size();
            this.audios += msg.attachments.audios.size();
            this.videos += msg.attachments.videos.size();
            this.docs += msg.attachments.docs.size();
            this.walls += msg.attachments.walls.size();
            this.stickers += msg.attachments.stickers.size();
            this.gifts += msg.attachments.gifts.size();
            this.links += msg.attachments.links.size();
            this.voices += msg.attachments.voices.size();
            this.calls += msg.attachments.calls.size();
            this.attachments += msg.attachments.size();

            if (!msg.attachments.docs.isEmpty()) {
                for (int i = 0; i < msg.attachments.docs.size(); i++) {
                    docsSize += msg.attachments.docs.get(i).size;
                }
            }
            if (!msg.attachments.voices.isEmpty()) {
                for (AudioMessage voice : msg.attachments.voices) {
                    voicesDuration += voice.duration;
                }
            }
            if (!msg.attachments.calls.isEmpty()) {
                for (Call call : msg.attachments.calls) {
                    callsDuration += call.duration;
                }
            }
            if (!msg.attachments.audios.isEmpty()) {
                for (Audio audio : msg.attachments.audios) {
                    audiosDuration += audio.duration;
                }
            }
            if (!msg.attachments.photos.isEmpty()) {
                for (Photo photo : msg.attachments.photos) {
                    if (photo.has_geo) geoPoints++;
                }
            }
        }
        if (msg.fwd_messages != null) {
            this.forwards += msg.fwd_messages.size();
            for (Message fwd : msg.fwd_messages) {
                countAttachments(fwd);
            }
        }
    }

    public static void resetIgnoreList() {
        changeIgnoreList(DEFAULT_IGNORE_LIST);
    }

    public static HashSet<String> getIgnoreSet() {
        return ignore;
    }

    public static ArrayList<String> getIgnoreList() {
        return new ArrayList<>(ignore);

    }

    public static void deleteIgnoreWord(String word) {
        ignore.remove(word);
        SettingsStore.putValue(KEY_MSG_IGNORE_LIST, ignore);
    }

    public static void changeIgnoreList(List<String> list) {
        ignore.clear();
        ignore.addAll(list);
        SettingsStore.putValue(KEY_MSG_IGNORE_LIST, ignore);
    }

    private static HashSet<String> loadIgnoreExplicit() {
        HashSet<String> set = new HashSet<>();
        try {
            String content = AndroidUtil.loadAssestsFile(AppContext.context, "ignore_explicit.txt");
            Matcher matcher = whitespace.get().matcher(content);
            while (matcher.find()) {
                set.add(matcher.group());
            }

            return set;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static HashSet<String> loadIgnoreList() {
        return new HashSet<>(SettingsStore.getStringSet(KEY_MSG_IGNORE_LIST, new HashSet<>(DEFAULT_IGNORE_LIST)));
    }
}
