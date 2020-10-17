package ru.euphoria.doggy.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.util.AndroidUtil;

public class EditTextDialog extends MaterialAlertDialogBuilder {
    private TextInputLayout input;
    private TextInputEditText text;

    public EditTextDialog(Context context) {
        super(context);

        LinearLayout root = new LinearLayout(context);
        AndroidUtil.setDialogContentPadding(root);

        input = (TextInputLayout) LayoutInflater
                .from(context).inflate(R.layout.text_input_layout, root, false);
        text = input.findViewById(R.id.input_text);
        root.addView(input);

        setTitle("Добавить слово");
        setView(root);
    }

    public TextInputLayout getInput() {
        return input;
    }

    public AppCompatEditText getEditText() {
        return text;
    }
}
