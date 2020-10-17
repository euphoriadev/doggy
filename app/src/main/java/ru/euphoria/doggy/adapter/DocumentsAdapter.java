package ru.euphoria.doggy.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.R;
import ru.euphoria.doggy.api.model.Document;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.FileUtil;

public class DocumentsAdapter extends BaseAdapter<DocumentsAdapter.ViewHolder, Document> {
    private static final ArrayList<IconPlaceholder> placeholders = new ArrayList<>();
    static {
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_android, R.color.green_50, R.color.green_400, "apk"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_lang_python, R.color.amber_50, R.color.amber_400, "py"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_file_pdf, R.color.red_50, R.color.red_400, "pdf"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_key_varian, R.color.teal_50, R.color.teal_400, "jks"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_file_powerpoint, R.color.red_50, R.color.red_400, "pptx"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_file_excel, R.color.green_50, R.color.green_500, "xls"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_file_file_delimited, R.color.green_50, R.color.green_500, "csv"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_book_account, R.color.indigo_50, R.color.indigo_400, "vcf"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_xml, R.color.brown_50, R.color.brown_400, "xml"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_font, R.color.deep_orange_50, R.color.deep_orange_400, "ttf"));

        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_windows, R.color.light_blue_50, R.color.light_blue_400, "exe"));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_apple, R.color.gray_100, R.color.gray_500, "dmg"));


        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_text_box, R.color.blue_50, R.color.blue_400, FileUtil.docs, -1));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_code_braces, R.color.brown_50, R.color.brown_400, FileUtil.code, -1));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_database, R.color.blue_gray_50, R.color.blue_gray_400, FileUtil.databases, -1));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_zip_box, R.color.purple_50, R.color.purple_400, FileUtil.archive, Document.TYPE_ARCHIVE));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_outline_movie, R.color.pink_50, R.color.pink_400, FileUtil.video, Document.TYPE_VIDEO));
        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_audiotrack, R.color.light_blue_50, R.color.light_blue_400, FileUtil.audio, Document.TYPE_AUDIO));

        placeholders.add(new IconPlaceholder(R.drawable.ic_vector_outline_book, R.color.lime_50, R.color.lime_500, Document.TYPE_BOOK));
    }

    private DateFormat dateFormat;

    public DocumentsAdapter(Context context, List<Document> values) {
        super(context, values);
        dateFormat = SimpleDateFormat.getDateInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_doc,
                parent, false);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document doc = getItem(position);
        String size = Formatter.formatFileSize(getContext(), doc.size);
        String date = dateFormat.format(doc.date * 1000L);

        holder.title.setText(doc.title);
        holder.summary.setText(String.format("%s • %s", size, date));
        holder.summary.setText(AndroidUtil.join(" • ", size, doc.ext, date));

        holder.image.setImageDrawable(null);
        holder.image.setImageTintList(null);
        holder.image.setBackgroundTintList(null);
        holder.image.setScaleType(ImageView.ScaleType.CENTER);
        holder.imageText.setVisibility(View.GONE);

        if (ArrayUtil.isNotEmpty(doc.photo_sizes)) {
            holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            AndroidUtil.loadImage(holder.image, doc.photo_sizes.get(0).src);
        } else {
            IconPlaceholder icon = findIcon(doc);
            if (icon != null) {
                holder.image.setImageResource(icon.iconRes);
                holder.image.setBackgroundTintList(ColorStateList.valueOf(icon.backgroundColor));
                holder.image.setImageTintList(ColorStateList.valueOf(icon.iconColor));
            } else {
                holder.imageText.setVisibility(View.VISIBLE);
                holder.imageText.setText(doc.ext.toUpperCase());
            }
        }

        holder.overflow.setOnClickListener(v -> {
            if (overflowClickListener != null) {
                overflowClickListener.onClick(holder.overflow, position);
            }
        });
    }

    private IconPlaceholder findIcon(Document doc) {
        for (IconPlaceholder icon : placeholders) {
            for (String e : icon.ext) {
                if (doc.ext.startsWith(e)) {
                    return icon;
                }
            }
            if (doc.type == icon.docType) {
                return icon;
            }

            if (icon.extPattern != null) {
                Matcher matcher = icon.extPattern.matcher(doc.ext);
                if (matcher.find()) {
                    return icon;
                }
            }
        }
        return null;
    }

    public static class IconPlaceholder {
        public int iconRes;
        public int backgroundColor;
        public int iconColor;
        public String[] ext;
        public int docType;
        private Pattern extPattern;

        public IconPlaceholder(int iconRes, @ColorRes int backgroundColor, @ColorRes int iconColor, String[] ext, int docType) {
            this.iconRes = iconRes;
            this.backgroundColor = ContextCompat.getColor(AppContext.context, backgroundColor);
            this.iconColor = ContextCompat.getColor(AppContext.context, iconColor);
            this.ext = ext;
            this.docType = docType;
        }

        public IconPlaceholder(int iconRes, @ColorRes int backgroundColor, @ColorRes int iconColor, Pattern ext, int docType) {
            this(iconRes, backgroundColor, iconColor, new String[]{}, docType);
            setExtPattern(ext);
        }

        public IconPlaceholder(int iconRes, @ColorRes int backgroundColor, @ColorRes int iconColor, String ext) {
            this(iconRes, backgroundColor, iconColor, new String[]{ext}, -1);
        }

        public IconPlaceholder(int iconRes, @ColorRes int backgroundColor, @ColorRes int iconColor, int docType) {
            this(iconRes, backgroundColor, iconColor, new String[]{}, docType);
        }

        public IconPlaceholder setExtPattern(Pattern p) {
            this.extPattern = p;
            return this;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.doc_image) ImageView image;
        @BindView(R.id.doc_title) TextView title;
        @BindView(R.id.doc_summary) TextView summary;
        @BindView(R.id.doc_image_text) TextView imageText;
        @BindView(R.id.overflow) ImageView overflow;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
