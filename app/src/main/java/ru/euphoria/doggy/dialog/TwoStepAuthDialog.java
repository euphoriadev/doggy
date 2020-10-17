package ru.euphoria.doggy.dialog;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.util.AndroidUtil;

public class TwoStepAuthDialog extends MaterialAlertDialogBuilder {
    private EditText editText;

    public TwoStepAuthDialog(Context context) {
        super(context);
        this.editText = createCodeText(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputLayout input = new TextInputLayout(context);
        input.addView(editText);

        TextView text = new TextView(context);
        text.setTextSize(16);
        text.setTextColor(AndroidUtil.getAttrColor(context, android.R.attr.textColorPrimary));
        text.setText(R.string.two_factor_confirm);
        text.setPadding(0, 0, 0, (int) AndroidUtil.px(context, 16));

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);
        AndroidUtil.setDialogContentPadding(layout);
        layout.addView(text);
        layout.addView(input);

        setTitle(R.string.start_auth);
        setView(layout);
        setNegativeButton(android.R.string.cancel, null);
    }

    public String getCode() {
        return editText.getText().toString();
    }

    private static EditText createCodeText(Context context) {
        EditText editText = new EditText(context);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        return editText;
    }
}
