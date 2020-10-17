package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.method.ParamSetter;
import ru.euphoria.doggy.json.SpannedJsonObject;
import ru.euphoria.doggy.util.AndroidUtil;

@SuppressLint("CheckResult")
public class ResponseActivity extends BaseActivity {
    private ContentLoadingProgressBar progress;
    private ListView response;
    private String body;

    private ParamSetter setter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);
        getSupportActionBar().setTitle(R.string.title_response);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        response = findViewById(R.id.response);
        progress = findViewById(R.id.progress);
        progress.show();

        sendRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_response, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_copy:
                AndroidUtil.copyText(this, body);
                break;
            case R.id.item_link:
                AndroidUtil.copyText(this, getLink());
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayText(String body) throws Exception {
        this.body = body;

        SpannedJsonObject json = new SpannedJsonObject(body);
        ArrayList<CharSequence> sequences = json.getCharSequences(4);
        response.setAdapter(new ArrayAdapter<>(this, R.layout.list_response_textview,
                sequences));

        YandexMetrica.reportEvent("Кастомный запрос");
    }

    @SuppressWarnings("unchecked")
    private String getLink() {
        HttpUrl.Builder url = setter.request().url().newBuilder();
        HashMap<String, String> params = (HashMap<String, String>) getIntent().getSerializableExtra("params");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.addQueryParameter(entry.getKey(), entry.getValue());
        }

        return url.toString();
    }

    @SuppressWarnings("unchecked")
    private void sendRequest() {
        Intent intent = getIntent();
        String method = intent.getStringExtra("method");
        HashMap<String, String> params = (HashMap<String, String>) intent.getSerializableExtra("params");
        setter = VKApi.createParamSetter(method);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            setter.put(entry.getKey(), entry.getValue());
        }

        getSupportActionBar().setSubtitle(method);
        Single.fromCallable(setter::body)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(progress::hide)
                .subscribe(this::displayText, AndroidUtil.handleError(this));
    }
}
