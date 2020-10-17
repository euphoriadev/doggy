package ru.euphoria.doggy.dialog;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import ru.euphoria.doggy.R;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.util.AndroidUtil;

public class AuthDialog extends MaterialAlertDialogBuilder {
    private EditText login;
    private EditText password;
    private CheckBox checkBox;

    public AuthDialog(Context context) {
        super(context);
        this.login = createLoginText(context);
        this.password = createPasswordText(context);
        this.checkBox = createCheckBox(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputLayout inputTextLogin = new TextInputLayout(context);
        inputTextLogin.addView(login);

        TextInputLayout inputTextPassword = new TextInputLayout(context);
        inputTextPassword.addView(password);

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);
        AndroidUtil.setDialogContentPadding(layout);

        layout.addView(inputTextLogin);
        layout.addView(inputTextPassword);
        layout.addView(checkBox);

        setTitle(R.string.start_auth);
        setView(layout);
        setNegativeButton(android.R.string.cancel, null);
    }

    public String getLogin() {
        return login.getText().toString();
    }

    public String getPassword() {
        return password.getText().toString();
    }

    public static CheckBox createCheckBox(Context context) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(R.string.save_password);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                -> SettingsStore.putValue(SettingsStore.KEY_CHECK_SAVE_PASSWORD, isChecked));
        return checkBox;
    }

    private static EditText createLoginText(Context context) {
        EditText text = new EditText(context);
        text.setHint(R.string.text_label_login);

        String login = SettingsStore.getLogin();
        if (!TextUtils.isEmpty(login)) {
            text.setText(login);
        }
        return text;
    }

    private static EditText createPasswordText(Context context) {
        EditText text = new EditText(context);
        text.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        text.setHint(R.string.text_label_password);

        if (SettingsStore.getBoolean(SettingsStore.KEY_CHECK_SAVE_PASSWORD)) {
            text.setText(SettingsStore.getPassword());
            text.requestFocus();
        }
        return text;
    }
}
