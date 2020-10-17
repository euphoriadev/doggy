package ru.euphoria.doggy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Random;

import butterknife.BindView;

/**
 * Created by admin on 24.03.18.
 */

public class StartActivity extends BaseActivity {
    private static final int REQUEST_CODE_AUTH = 100;
    private static final int DURATION = 2000;
    private static final Random random = new Random();
    private static final String[] messages = {
            "Множество функций в одном месте",
            "А ты думал в сказку попал?",
            "Знает о тебе больше, чем ты сам",
            "Жду поддержки только от позвоночника",
            "Как ты это бровями делаешь?",
            "Пей кофе от перегара",
            "Как робот пылесос узнает куда ехать?",
            "Родной брат сына маминой подруги",
            "Мем смешной, а ситуация страшная",
            "Скажи это моим бубенчикам",
            "Ты как мой носок - я тебя давно искал",
            "Запиваю алкоголь водкой",
            "GUSSI",
            "-Апути\n-Нипутю. Кусь",
            "Doggy dog ebashy ya hotdog",
            "Демо версия тупых шуток"
    };

    @BindView(R.id.summary) TextView summary;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        startPulse();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void onAuthClick(View v) {
        startActivityForResult(new Intent(this,
                LoginActivity.class), REQUEST_CODE_AUTH);
    }

    private void startPulse() {
        if (isDestroyed()) return;

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> summary.animate()
                .alpha(0)
                .withLayer()
                .setDuration(500)
                .withEndAction(() -> {
                    summary.setText(messages[random.nextInt(messages.length)]);

                    summary.animate()
                            .alpha(1)
                            .withLayer()
                            .setDuration(500)
                            .withEndAction(this::startPulse);
                }), DURATION);
    }
}
