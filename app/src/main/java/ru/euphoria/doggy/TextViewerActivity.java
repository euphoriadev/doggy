package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import butterknife.BindView;
import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.classifier.CodeProcessor;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.api.model.Document;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.FileUtil;

public class TextViewerActivity extends BaseActivity {
    static {
        // train classifier on app start
        CodeProcessor.init(AppContext.context);
    }
    public static void start(Context context, Document doc) {
        Intent starter = new Intent(context, TextViewerActivity.class);
        starter.putExtra("doc", doc);
        context.startActivity(starter);
    }

    @BindView(R.id.text) TextView text;
    @BindView(R.id.progress) ContentLoadingProgressBar progress;
    @BindView(R.id.scroll_view) ScrollView scroll;
    @BindView(R.id.code_view) CodeView code;

    private Document doc;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);
        doc = getIntent().getParcelableExtra("doc");

        getSupportActionBar().setTitle(doc.title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadText();
    }

    private void setText(String s) {
        if (FileUtil.code.matcher(s).find()) {
            code.setVisibility(View.VISIBLE);

            Options options = Options.Default.get(this)
                    .withLanguage(doc.ext)
                    .withCode(s)
                    .withTheme(ColorTheme.DEFAULT)
                    .disableHighlightAnimation();
            code.setOptions(options);
        } else {
            scroll.setVisibility(View.VISIBLE);
            text.setText(s);
        }
    }

    @SuppressLint("CheckResult")
    private void loadText() {
        progress.show();

        AndroidUtil.request(doc.url)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(progress::hide)
                .subscribe(this::setText);
    }
}
