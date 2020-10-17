package ru.euphoria.doggy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;
import io.reactivex.functions.Predicate;
import ru.euphoria.doggy.adapter.PhonesAdapter;
import ru.euphoria.doggy.adapter.WrapLinearLayoutManager;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.db.AppDatabase;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.UserUtil;

/**
 * Created by admin on 21.05.18.
 */
@SuppressLint("CheckResult")
public class PhonesActivity extends BaseActivity {
    private PhonesAdapter adapter;
    private RecyclerView recycler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phones);
        getSupportActionBar().setTitle(R.string.friends_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView.LayoutManager layoutManager = new WrapLinearLayoutManager(this);
        recycler = findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);

        if (!AndroidUtil.hasConnection()) {
            Toast.makeText(this, R.string.error_connection, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        AppDatabase.database().users().friends().observe(this, this::createAdapter);

        UserUtil.getFriends(this)
                .subscribe(users -> {
                    for (User user : users) user.is_friend = true;

                    AppDatabase.database().users().insert(users);
                }, AndroidUtil.handleError(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_phones, menu);

        MenuItem itemSearch = menu.findItem(R.id.item_search);

        searchView = (SearchView) itemSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.search(query);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_sync:
                onSync();
                break;

            case R.id.item_export_csv:
                createExportCsvDialog();
                break;

            case R.id.item_export_vcf:
                exportToVcf();
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertContact(User user) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, user.toString())
                .build());

        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE)
                .withValue(Nickname.NAME, user.nickname)
                .build());

        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .withValue(Phone.NUMBER, user.mobile_phone)
                .build());

        ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.TYPE, Phone.TYPE_HOME)
                .withValue(Phone.NUMBER, user.home_phone)
                .build());

        if (!TextUtils.isEmpty(user.photo_200)) {
            try {
                Bitmap bitmap = Picasso.get().load(user.photo_200).get();
                if (bitmap != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(bitmap.getByteCount());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bos);

                    ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                            .withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                            .withValue(Photo.PHOTO, bos.toByteArray())
                            .build());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void onSync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_CONTACTS}, 100);
                return;
            }
        }
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Sync...");
        dialog.setMax(adapter.getItemCount());
        dialog.setProgress(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        new Thread(() -> {
            List<User> users = adapter.getValues();
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                if (UserUtil.validatePhone(user.mobile_phone)
                        || UserUtil.validatePhone(user.home_phone)) {
                    insertContact(user);
                }

                final int progress = i;
                runOnUiThread(() -> dialog.setProgress(progress));
            }

            runOnUiThread(() -> {
                dialog.dismiss();
                Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show();
                YandexMetrica.reportEvent("Синхронизация контактов");
            });
        }).start();
    }

    public void onUserClick(User user) {
        ArrayList<Pair<String, String>> connections =
                UserUtil.getConnections(user);
        if (connections.isEmpty()) {
            openContact(user);
        } else {
            ArrayList<String> items = new ArrayList<>();
            items.add(getString(R.string.open_contact));

            for (Pair<String, String> connection : connections) {
                items.add(connection.first);
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(user.toString());
            builder.setItems(items.toArray(new String[]{}), (dialog, which) -> {
                switch (which) {
                    case 0:
                        openContact(user);
                        break;

                    default:
                        Pair<String, String> pair = connections.get(which - 1);
                        browse(pair.first, pair.second);
                }
            });
            builder.show();
        }
    }

    private void browse(String type, String value) {
        switch (type) {
            case "Skype":
                AndroidUtil.openSkype(this, value);
                break;

            case "Facebook":
                AndroidUtil.browseFacebook(this, value);
                break;

            case "Twitter":
                AndroidUtil.browseTwitter(this, value);
                break;

            case "Instagram":
                AndroidUtil.browseInstagram(this, value);
                break;
        }
    }

    private void openContact(User user) {
        Intent intent = new Intent(Intents.SHOW_OR_CREATE_CONTACT, Uri.parse("tel:" + user.mobile_phone));
        intent.putExtra(Insert.NAME, user.toString());
        intent.putExtra(Insert.PHONE, user.mobile_phone);
        intent.putExtra(Intents.EXTRA_FORCE_CREATE, true);

        ArrayList<ContentValues> data = new ArrayList<>();
        if (!TextUtils.isEmpty(user.home_phone)
                && user.home_phone.matches(".*\\d+.*")) {
            data.add(appendContactNumber(Phone.TYPE_HOME, user.home_phone));
        }

        new Thread(() -> {
            try {
                String path = ArrayUtil.firstNotEmpty(user.photo_200, user.photo_100, user.photo_50);
                if (!TextUtils.isEmpty(path)) {
                    Bitmap bitmap = Picasso.get().load(path).get();
                    if (bitmap != null) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(bitmap.getByteCount());
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                        data.add(appendContactRow(Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, bos.toByteArray()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            intent.putParcelableArrayListExtra(Insert.DATA, data);
            startActivity(Intent.createChooser(intent, "Show contact"));

        }).start();
    }

    private ContentValues appendContactNumber(int type, String number) {
        ContentValues cv = new ContentValues();
        cv.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        cv.put(Phone.TYPE, type);
        cv.put(Phone.NUMBER, number);

        return cv;
    }

    private ContentValues appendContactRow(String type, String name, String value) {
        ContentValues row = new ContentValues();
        row.put(Data.MIMETYPE, type);
        row.put(name, value);

        return row;
    }

    private ContentValues appendContactRow(String type, String name, byte[] value) {
        ContentValues row = new ContentValues();
        row.put(Data.MIMETYPE, type);
        row.put(name, value);

        return row;
    }

    private void createExportCsvDialog() {
        if (!AndroidUtil.checkStoragePermissions(this)) {
            return;
        }
        String[] fields = getResources().getStringArray(R.array.csv_fields);
        boolean[] checked = new boolean[fields.length];
        Arrays.fill(checked, true);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choice_csv_fields)
                .setMultiChoiceItems(fields, checked, (dialog, which, isChecked) -> {
                    checked[which] = isChecked;
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    exportToCsv(fields, checked);
                })
                .show();

    }

    private void exportToVcf() {
        ContactsExporter exporter = new VcfContactsExported(adapter.getValues());
        processExport(exporter, "Error export csv");
    }

    private void exportToCsv(String[] fields, boolean[] checked) {
        ContactsExporter exporter = new CsvContactsExporter(adapter.getValues(), fields, checked);
        processExport(exporter, "Error export csv");
    }

    private void processExport(ContactsExporter exporter, String error) {
        try {
            exporter.export();
            successExport(exporter);
        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtil.toast(this, e.getMessage());
            YandexMetrica.reportError(error, e);
        }
    }

    private void successExport(ContactsExporter exporter) {
        Snackbar snackbar = AndroidUtil.snackbar(this, getString(R.string.file_saved_in) + exporter.file().toString(), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.show, v -> {
            AndroidUtil.openFolder(this, exporter.file().getParentFile());
        }).show();

        HashMap<String, Object> map = new HashMap<>();
        map.put("Формат", exporter.prefix());
        YandexMetrica.reportEvent("Экспорт контактов", map);
    }

    private void createAdapter(List<User> users) {
        Predicate<User> filter = user -> !UserUtil.getConnections(user).isEmpty()
                || UserUtil.validatePhone(user.home_phone)
                || UserUtil.validatePhone(user.mobile_phone);

        ArrayUtil.filter(users, filter);

        adapter = new PhonesAdapter(this, users);
        recycler.setAdapter(adapter);

        adapter.setOnClickListener(v -> {
            int position = recycler.getChildAdapterPosition(v);
            User item = adapter.getItem(position);
            onUserClick(item);
        });

        getSupportActionBar().setTitle(getString(R.string.friends_contacts)
                + (" (" + adapter.getItemCount()
                + ")"));
    }

    private static class VcfContactsExported extends ContactsExporter {
        public VcfContactsExported(List<User> users) {
            super(users);
        }

        @Override
        public String prefix() {
            return "vcf";
        }

        @Override
        public void onExport(File file) throws Exception {
            StringBuilder builder = new StringBuilder();
            for (User user : users) {
                builder.append(toVcard(user));
            }

            FileWriter writer = new FileWriter(file);
            writer.write(builder.toString());
            writer.close();
        }

        private static String toVcard(User user) {
            if ("".equals(user.mobile_phone)) {
                return "";
            }

            StringBuilder buffer = new StringBuilder();
            buffer.append("BEGIN:VCARD\n");
            buffer.append("VERSION:3.0\n");
            buffer.append("FM:").append(user.toString()).append("\n");
            buffer.append("N:").append(user.first_name).append(";;\n");
            buffer.append("TEL;type=CELL;type=VOICE: ").append("+").append(UserUtil.formatNumber(user.mobile_phone)).append("\n");
            buffer.append("END:VCARD\n");
            return buffer.toString();
        }
    }

    private static class CsvContactsExporter extends ContactsExporter {
        private String[] fields;
        private boolean[] checked;

        public CsvContactsExporter(List<User> users, String[] fields, boolean[] checked) {
            super(users);
            this.fields = fields;
            this.checked = checked;
        }

        @Override
        public String prefix() {
            return "csv";
        }

        @Override
        public void onExport(File file) throws Exception {
            CsvWriter writer = new CsvWriter();
            writer.setAlwaysDelimitText(true);

            try (CsvAppender appender = writer.append(file, StandardCharsets.UTF_8)) {
                for (int i = 0; i < checked.length; i++) {
                    if (checked[i]) {
                        appender.appendField(fields[i]);
                    }
                }
                appender.endLine();

                for (User user : users) {
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) {
                            switch (i) {
                                case 0:
                                    appender.appendField(String.valueOf(user.id));
                                    break;
                                case 1:
                                    appender.appendField(user.first_name);
                                    break;
                                case 2:
                                    appender.appendField(user.last_name);
                                    break;
                                case 3:
                                    appender.appendField(UserUtil.formatNumber(user.mobile_phone));
                                    break;
                                case 4:
                                    appender.appendField(UserUtil.formatNumber(user.home_phone));
                                    break;
                                case 5:
                                    appender.appendField(user.skype);
                                    break;
                                case 6:
                                    appender.appendField(user.facebook);
                                    break;
                                case 7:
                                    appender.appendField(user.twitter);
                                    break;
                                case 8:
                                    appender.appendField(user.instagram);
                                    break;
                            }
                        }
                    }
                    appender.endLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private static abstract class ContactsExporter {
        public List<User> users;

        public ContactsExporter(List<User> users) {
            this.users = users;
        }

        public void export() throws Exception {
            onExport(file());
        }

        public abstract void onExport(File file) throws Exception;

        public String prefix() {
            return "";
        }

        public File file() {
            return new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS),
                    String.format(Locale.ROOT, "contacts-%s.%s",
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(System.currentTimeMillis())), prefix()));
        }
    }
}
