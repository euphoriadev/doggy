package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.yandex.metrica.YandexMetrica;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.AudioUtil;
import ru.euphoria.doggy.yandex.Translator;

@SuppressLint("CheckResult")
public class LyricsActivity extends BaseActivity {
    private String original, translated;
    private Audio audio;
    private ContentLoadingProgressBar progressBar;
    private TextView lyrics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        audio = getIntent().getParcelableExtra("audio");
        lyrics = findViewById(R.id.textLyrics);
        progressBar = findViewById(R.id.progress);

        getSupportActionBar().setTitle(audio.title);
        getSupportActionBar().setSubtitle(audio.artist);
        findLyrics();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lyrics, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_translate:
                translate();
        }
        return super.onOptionsItemSelected(item);
    }

    private void findLyrics() {
        if (!AndroidUtil.hasConnection()) {
            return;
        }
        progressBar.show();

        Single<String> single;
        if (audio.lyrics_id > 0) {
            single = AudioUtil.getLyrics(audio);
        } else {
            single = AudioUtil.findLyrics(audio);
            YandexMetrica.reportEvent("Поиск текста песни");
        }

        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(value -> AndroidUtil.hasConnection())
                .subscribe(value -> {
                    progressBar.hide();
                    if (!TextUtils.isEmpty(value)) {
                        lyrics.setText(value);
                    } else {
                        noLyricsView();
                    }
                }, AndroidUtil.handleError(this));
    }

    private void noLyricsView() {
        ViewGroup container = findViewById(R.id.container);
        View view = View.inflate(this, R.layout.view_no_lyrics, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        container.addView(view);

        view.findViewById(R.id.button_find_lyrics)
                .setOnClickListener(v -> {
                    AndroidUtil.search(this, audio.toString() + " lyrics");
                });
    }

    private void translate() {
        String text = lyrics.getText().toString();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        if (translated == null) {
            Translator.translate(text)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterSuccess(response -> {
                        this.original = text;
                        this.translated = response;
                    })
                    .subscribe(lyrics::setText, AndroidUtil.handleError(this));
        } else {
            lyrics.setText(text.equals(translated) ? original : translated);
        }
    }
}
