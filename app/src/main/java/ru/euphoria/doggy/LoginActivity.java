package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import ru.euphoria.doggy.api.Authorizer;
import ru.euphoria.doggy.api.ErrorCodes;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.VKException;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.data.SettingsStore;
import ru.euphoria.doggy.dialog.CaptchaDialog;
import ru.euphoria.doggy.dialog.TwoStepAuthDialog;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.AudioUtil;
import ru.euphoria.doggy.util.UserUtil;

/**
 * Created by admin on 24.03.18.
 */

public class LoginActivity extends BaseActivity {
    @BindView(R.id.login) AppCompatEditText login;
    @BindView(R.id.password) AppCompatEditText password;
    @BindView(R.id.loginLayout) TextInputLayout loginLayout;
    @BindView(R.id.passwordLayout) TextInputLayout passwordLayout;
    @BindView(R.id.button_auth_token) Button authToken;

    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle(R.string.auth);

    }

    @OnClick(R.id.auth)
    void submit() {
        if (!AndroidUtil.hasConnection()) {
            AndroidUtil.toastErrorConnection(this);
            return;
        }

        if (checkEmptyLabel(loginLayout, login, getString(R.string.empty_login_error)) ||
                checkEmptyLabel(passwordLayout, password, getString(R.string.empty_password_error))) {
            return;
        }

        showProgress();
        directLogin(login.getText().toString(), password.getText().toString(), null, null, null);
    }

    @SuppressWarnings("CheckResult")
    private void directLogin(String login, String password, String code, String captchaSid, String captchaKey) {
        String url = Authorizer.getDirectUrl(2685278, "lxhD8OD7dMsqtXIm5IUY", login, password, AndroidUtil.getKateVersionApi(), code, captchaSid, captchaKey);
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", AndroidUtil.getKateUserAgent())
                .build();

        AndroidUtil.request(request)
                .map(JSONObject::new)
                .flatMap(json -> {
                    VKApi.checkError(json, url);

                    String token = json.optString("access_token");
                    SettingsStore.putValue(SettingsStore.KEY_ACCESS_TOKEN, token);
                    refreshToken(token);

                    User me = UserUtil.getUser().blockingGet();
                    SettingsStore.putValue(SettingsStore.KEY_USER_ID, me.id);

                    return Single.just(token);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(token -> {
                    AndroidUtil.toast(this, R.string.success);

                    hideProgress();
                    setResult(RESULT_OK);
                    finish();
                }, error -> {
                    error.printStackTrace();
                    if (error instanceof VKException) {
                        VKException ex = (VKException) error;
                        if (ex.getMessage().contains("sms sent")
                                || ex.getMessage().contains("app code")) {
                            TwoStepAuthDialog codeDialog = new TwoStepAuthDialog(this);
                            codeDialog.setPositiveButton(android.R.string.ok, (dialog, which)
                                    -> directLogin(login, password, codeDialog.getCode(), null, null));
                            codeDialog.show();
                        } else if (ex.code == ErrorCodes.CAPTCHA_NEEDED) {
                            CaptchaDialog captcha = new CaptchaDialog(this, ex);
                            captcha.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                directLogin(login, password, code, ex.captchaSid, captcha.getText());
                            });
                            captcha.show();
                        } else {
                            AndroidUtil.toast(LoginActivity.this, error.getMessage());
                        }
                    }
                    hideProgress();
                });
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.button_auth_token)
    public void onAuthClick(View v) {
        TextInputLayout input = new TextInputLayout(this);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        input.setHint("Access token");

        AndroidUtil.setDialogContentPadding(input);

        EditText edit = new EditText(this);
        input.addView(edit);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.auth);
        builder.setView(input);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String token = edit.getText().toString();

            Flowable.fromCallable(() -> {
                JSONObject json = VKApi.users().get()
                        .fields(User.DEFAULT_FIELDS)
                        .accessToken(token)
                        .json();
                return VKApi.from(User.class, json).get(0);

            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> toast(throwable.getMessage()))
                    .subscribe(user -> {
                        SettingsStore.putValue(SettingsStore.KEY_USER_ID, user.id);
                        SettingsStore.putValue(SettingsStore.KEY_ACCESS_TOKEN, token);

                        toast("Вы вошли как " + user);
                        setResult(RESULT_OK);
                        finish();
                    }, error -> {
                        toast(error.getMessage());
                    });
        });

        builder.show();
    }

    private void refreshToken(String token) throws Exception {
        String refresh = VKApi.auth().refreshToken(AudioUtil.getReceipt())
                .accessToken(token)
                .v(AndroidUtil.getKateVersionApi())
                .userAgent(AndroidUtil.getKateUserAgent())
                .json()
                .optJSONObject("response").getString("token");

        SettingsStore.putValue(SettingsStore.KEY_AUDIO_ACCESS_TOKEN, refresh);
    }

    private boolean checkEmptyLabel(TextInputLayout label, AppCompatEditText text, String error) {
        label.setErrorEnabled(text.getText().length() == 0);
        if (label.isErrorEnabled()) {
            label.setError(error);
            return true;
        }
        return false;
    }

    private void showProgress() {
        if (isDestroyed()) {
            return;
        }
        dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.auth);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
    }

    private void hideProgress() {
        if (isDestroyed()) {
            return;
        }
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
