package ru.euphoria.doggy;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.euphoria.doggy.adapter.DocumentsAdapter;
import ru.euphoria.doggy.api.model.Document;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.FileUtil;

public class DocumentsFragment extends BaseAttachmentsFragment<Document> {
    private DocumentsAdapter adapter;

    public static DocumentsFragment newInstance(int peer) {
        Bundle args = new Bundle();
        args.putInt("peer", peer);

        DocumentsFragment fragment = new DocumentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        Disposable subscribe = flowable.subscribe(docs -> {
            adapter = new DocumentsAdapter(getActivity(), docs);
            recycler.setAdapter(adapter);

            adapter.setOnClickListener(v -> {
                int position = recycler.getChildAdapterPosition(v);
                Document item = adapter.getItem(position);
                if (ArrayUtil.isNotEmpty(item.photo_sizes)) {
                    PhotoViewerActivity.start(getActivity(), item.url);
                } else {
                    int extType = FileUtil.getExtensionType(item.ext);
                    if (item.type == Document.TYPE_TEXT || extType == FileUtil.FILE_TYPE_CODE) {
                        TextViewerActivity.start(getActivity(), item);
                    } else {
                        AndroidUtil.browse(getActivity(), item.url, item.ext);
                    }
                }
            });
            adapter.setOverflowClickListener((v, position) -> {
                Document item = adapter.getItem(position);
                createOverflowMenu(v, item);
            });
        });
        disposable.add(subscribe);
        return root;
    }

    public boolean onOverflowClick(MenuItem item, Document doc) {
        switch (item.getItemId()) {
            case R.id.item_download:
                String fixedExtension = getFixedExtension(doc);
                if (fixedExtension.equals(doc.ext)) {
                    AndroidUtil.download(getActivity(), doc);
                } else {
                    suggestRenameExtensionDialog(doc, fixedExtension);
                }
                break;

            case R.id.item_copy_link:
                AndroidUtil.copyText(getActivity(), doc.url);
                break;
        }
        return true;
    }

    @Override
    public Flowable<List<Document>> getFlowable() {
        return AppDatabase.database().docs()
                .byPeer(getPeerId())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    private void showHelpDialog() {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.help)
                .setMessage(R.string.fix_doc_ext_help)
                .setPositiveButton("злюся", null)
                .show();
    }

    private void suggestRenameExtensionDialog(Document document, String fixedExt) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(document.title);
        builder.setMessage(Html.fromHtml(
                String.format(getString(R.string.fix_doc_ext_message), document.ext, fixedExt)
        ));
        builder.setNeutralButton(R.string.help, (dialog, which) -> showHelpDialog());
        builder.setNegativeButton(R.string.no, (dialog, which)
                -> AndroidUtil.download(getContext(), document));
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            String title = document.title.replace(String.format(".%s", document.ext), "");
            AndroidUtil.download(getContext(), document.url, title, fixedExt);
        });
        builder.show();
    }

    /**
     * Метод проверяет, нужно ли исправлять расширение файла у документа.
     * Для этого он проходит все паттерны на наличиня совпадений
     * и если нашел, то возвращает правильное расширение,
     * например .apk1 > apk
     */
    private String getFixedExtension(Document doc) {
        Pattern[] patterns = new Pattern[] {
                FileUtil.audio, FileUtil.video, FileUtil.image, FileUtil.archive,
                FileUtil.executable
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(doc.ext);
            if (matcher.find()) {
                String fixedExt = doc.ext.substring(matcher.start(), matcher.end());
                System.out.println("fixed ext: " + fixedExt);
                System.out.println("doc ext: " + doc.ext);

                return fixedExt;
            }
        }
        return doc.ext;
    }

    private void createOverflowMenu(View v, Document doc) {
        PopupMenu menu = new PopupMenu(getActivity(), v);
        menu.inflate(R.menu.menu_doc_overflow);
        menu.setOnMenuItemClickListener(item -> onOverflowClick(item, doc));
        menu.show();
    }
}
