package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.widget.PairLayout;

@SuppressLint("CheckResult")
public class RequestsActivity extends BaseActivity implements View.OnClickListener {
    private ArrayAdapter<String> paramsAdapter;
    private LinearLayout params;
    private AutoCompleteTextView method;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        getSupportActionBar().setTitle(R.string.item_requests);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        method = findViewById(R.id.method);
        method.setThreshold(2);
        method.setOnItemClickListener((parent, view, position, id)
                -> createParamsAdapter());
        createAdapter();

        params = findViewById(R.id.params);
        createParams();

        findViewById(R.id.addPair).setOnClickListener(this);
        findViewById(R.id.sendRequest).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_requests, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_refresh:
                method.setText("");
                params.removeAllViews();
                createParams();
                break;

            case R.id.item_help:
                String text = method.getText().toString();
                String url = text.length() > 0
                        ? "https://vk.com/dev/" + text
                        : "https://vk.com/dev/methods";

                AndroidUtil.browse(this, url);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addPair:
                params.addView(createPair());
                break;

            case R.id.sendRequest:
                sendRequest();
                break;
        }
    }

    private void sendRequest() {
        if (!AndroidUtil.hasConnection()) {
            AndroidUtil.toastErrorConnection(this);
            return;
        }

        HashMap<String, String> map = new HashMap<>(params.getChildCount());

        for (int i = 0; i < params.getChildCount(); i++) {
            PairLayout pair = (PairLayout) params.getChildAt(i);
            map.put(pair.getKeyText(), pair.getValueText());
        }
        String methodName = method.getText().length() > 0
                ? method.getText().toString()
                : method.getHint().toString();

        Intent intent = new Intent(this, ResponseActivity.class);
        intent.putExtra("method", methodName);
        intent.putExtra("params", map);
        startActivity(intent);
    }

    private void createParams() {
        params.addView(createPair("access_token", SettingsStore.getAudioAccessToken()));
        params.addView(createPair("v", "5.103"));
        params.addView(createPair());
    }

    private PairLayout createPair() {
        PairLayout pair = new PairLayout(this);
        pair.setOnClickListener(v -> params.removeView(pair));
        if (paramsAdapter != null) {
            pair.getKey().setThreshold(1);
            pair.getKey().setAdapter(paramsAdapter);
        }
        return pair;
    }

    private PairLayout createPair(String key, String value) {
        return createPair().setPair(key, value);
    }

    private void refreshParamsAdapter(ArrayAdapter<String> adapter) {
        for (int i = 0; i < params.getChildCount(); i++) {
            PairLayout pair = (PairLayout) params.getChildAt(i);
            pair.getKey().setThreshold(1);
            pair.getKey().setAdapter(adapter);
        }
    }

    private void createParamsAdapter() {
        AndroidUtil.request("https://vk.com/dev/" + method.getText().toString())
                .flatMap(body -> {
                    ArrayList<String> params = new ArrayList<>();

                    Pattern pattern = Pattern.compile("class=\"dev_param_name\" title=\"(\\D+?)\"");
                    Matcher matcher = pattern.matcher(body);
                    while (matcher.find()) {
                        params.add(matcher.group(1));
                    }

                    return Single.just(paramsAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, params));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::refreshParamsAdapter);
    }

    private void createAdapter() {
        AndroidUtil.request("https://vk.com/dev/methods")
                .flatMap(body -> {
                    ArrayList<String> methods = new ArrayList<>();

                    Pattern pattern = Pattern.compile("<a href=\"/dev/(.*?)\"");
                    Matcher matcher = pattern.matcher(body);
                    while (matcher.find()) {
                        methods.add(matcher.group(1));
                    }

                    return Single.just(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, methods));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter -> method.setAdapter(adapter), AndroidUtil.handleError(this));
    }
}
