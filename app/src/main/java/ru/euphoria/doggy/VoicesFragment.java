package ru.euphoria.doggy;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.euphoria.doggy.adapter.VoiceAdapter;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.api.model.AudioMessage;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.service.AudioPlayerService;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.yandex.SpeechKit;

public class VoicesFragment extends BaseAttachmentsFragment<AudioMessage> {
    private VoiceAdapter adapter;

    public static VoicesFragment newInstance(int peer) {
        Bundle args = new Bundle();
        args.putInt("peer", peer);

        VoicesFragment fragment = new VoicesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        Disposable subscribe = flowable.subscribe(voices -> {
            adapter = new VoiceAdapter(getActivity(), voices);
            recycler.setAdapter(adapter);

            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                AudioMessage item = adapter.getItem(position);

                ArrayList<Audio> list = new ArrayList<>(adapter.getItemCount());
                for (AudioMessage value : voices) {
                    Audio a = new Audio();
                    a.title = "Voice Message";
                    a.artist = VoiceAdapter.getOwnerName(value);
                    a.url = value.link_mp3;
                    list.add(a);
                }
                AudioPlayerService.play(getActivity(), list, position);

            });
            adapter.setOverflowClickListener((v, position) -> {
                AudioMessage item = adapter.getItem(position);
                createOverflowMenu(v, item);
            });
        });
        disposable.add(subscribe);
        return root;
    }

    @Override
    public Flowable<List<AudioMessage>> getFlowable() {
        return AppDatabase.database().voices()
                .byPeer(getPeerId())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    public void onOverflowClick(MenuItem item, AudioMessage msg) {
        switch (item.getItemId()) {
            case R.id.item_download:
                if (AndroidUtil.checkStoragePermissions(getActivity())) {
                    AndroidUtil.download(getActivity(), msg);
                }
                break;

            case R.id.item_voice_to_text:
                voiceToText(msg);
                break;
        }
    }

    private void voiceToText(AudioMessage msg) {
        if (!TextUtils.isEmpty(msg.transcript)) {
            createVoiceTextDialog(msg.transcript);
            return;
        }

        ProgressDialog progress = createProgressDialog();
        new Thread(() -> {
            updateMessage(progress, "Start downloading file...");
            File voice = new File(getContext().getCacheDir(), "voice.ogg");
            try {
                AndroidUtil.download(voice, msg.link_ogg);
                updateMessage(progress, "Success download. Processing speech engine...");
                String text = SpeechKit.getText(voice);
                getActivity().runOnUiThread(() -> {
                    progress.cancel();
                    createVoiceTextDialog(text);
                });

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(progress::cancel);
            }

        }).start();
    }

    private void updateMessage(ProgressDialog dialog, String msg) {
        getActivity().runOnUiThread(() -> dialog.setMessage(msg));
    }

    private void createVoiceTextDialog(String text) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(R.string.voice_msg);
        builder.setMessage(text);
        builder.setNegativeButton(R.string.copy_text, (dialog, which)
                -> AndroidUtil.copyText(getContext(), text));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.get_voice_text));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        return dialog;
    }

    private void createOverflowMenu(View v, AudioMessage msg) {
        PopupMenu menu = new PopupMenu(getActivity(), v);
        menu.inflate(R.menu.menu_voice_overflow);
        menu.setOnMenuItemClickListener(item -> {
            onOverflowClick(item, msg);
            return true;
        });
        menu.show();
    }
}
