package ru.euphoria.doggy.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import ru.euphoria.doggy.R;

public class PairLayout extends LinearLayout {
    private AutoCompleteTextView key;
    private AutoCompleteTextView value;

    public PairLayout(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_pair, this, true);

        key = findViewById(R.id.key);
        value = findViewById(R.id.value);
        AutofitHelper.create(key).setMinTextSize(12);
        AutofitHelper.create(value).setMinTextSize(12);
    }

    public PairLayout setPair(String key, String value) {
        this.key.setText(key);
        this.value.setText(value);
        return this;
    }

    public AutoCompleteTextView getKey() {
        return key;
    }

    public AutoCompleteTextView getValue() {
        return value;
    }

    public String getKeyText() {
        return key.getText().toString();
    }

    public String getValueText() {
        return value.getText().toString();
    }
}
