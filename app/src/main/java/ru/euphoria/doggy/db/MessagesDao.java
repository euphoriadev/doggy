package ru.euphoria.doggy.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import ru.euphoria.doggy.api.model.Attachment;
import ru.euphoria.doggy.api.model.Attachments;
import ru.euphoria.doggy.api.model.Message;

@Dao
public abstract class MessagesDao implements BaseDao<Message> {
    @Query("SELECT COUNT(*) FROM messages")
    public abstract int count();

    @Query("SELECT * FROM messages")
    public abstract Flowable<List<Message>> all();

    @Query("SELECT * FROM messages WHERE peer_id = :id")
    public abstract Flowable<List<Message>> byPeer(int id);

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    public abstract Message byId(int id);

    @Query("SELECT id, from_id, peer_id, text, date, emoji FROM messages WHERE peer_id = :id")
    public abstract Cursor cursorByPeer(int id);

    @Query("SELECT * FROM messages WHERE peer_id = :id BETWEEN :startTime AND :endTime")
    public abstract Cursor cursorByPeer(int id, long startTime, long endTime);

    @Transaction
    public void insertWithAttachments(List<Message> messages, int peer) {
        insert(messages);

        AppDatabase database = AppDatabase.database();
        for (Message msg : messages) {
            Attachments attachments = msg.attachments;
            if (attachments == null) {
                continue;
            }
            if (parsePeers(attachments.photos, peer, msg)) {
                database.photos().insert(attachments.photos);
            }
            if (parsePeers(attachments.docs, peer, msg)) {
                database.docs().insert(attachments.docs);
            }
            if (parsePeers(attachments.audios, peer, msg)) {
                database.audios().insert(attachments.audios);
            }
            if (parsePeers(attachments.videos, peer, msg)) {
                database.videos().insert(attachments.videos);
            }
            if (parsePeers(attachments.voices, peer, msg)) {
                database.voices().insert(attachments.voices);
            }
            if (parsePeers(attachments.calls, peer, msg)) {
                database.calls().insert(attachments.calls);
            }
            if (parsePeers(attachments.links, peer, msg)) {
                database.links().insert(attachments.links);
            }
            if (msg.fwd_messages != null) {
                insertWithAttachments(msg.fwd_messages, peer);
            }
        }
    }

    private boolean parsePeers(ArrayList<? extends Attachment> values, int peer_id, Message msg) {
        if (!values.isEmpty()) {
            for (Attachment value : values) {
                value.peer_id = peer_id;
                value.msg_id = msg.id;
                value.msg_date = msg.date;
            }
            return true;
        }
        return false;
    }
}
