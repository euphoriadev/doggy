package ru.euphoria.doggy.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ru.euphoria.doggy.AppContext;
import ru.euphoria.doggy.api.model.Album;
import ru.euphoria.doggy.api.model.Audio;
import ru.euphoria.doggy.api.model.AudioMessage;
import ru.euphoria.doggy.api.model.Call;
import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.api.model.Document;
import ru.euphoria.doggy.api.model.Link;
import ru.euphoria.doggy.api.model.LongPollEvent;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.Photo;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.api.model.Video;
import ru.euphoria.doggy.util.MessageStats;

@Database(entities = {
        User.class, Audio.class, Album.class,
        Message.class, Photo.class, Video.class,
        Document.class, AudioMessage.class, Call.class, Link.class,
        Community.class, MessageStats.class, LongPollEvent.class
}, version = 20, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static AppDatabase database() {
        return AppContext.database;
    }

    public abstract UsersDao users();

    public abstract AudiosDao audios();

    public abstract VideosDao videos();

    public abstract AlbumDao albums();

    public abstract VoicesDao voices();

    public abstract PhotosDao photos();

    public abstract DocumentsDou docs();

    public abstract CallsDao calls();

    public abstract LinksDao links();

    public abstract GroupsDao groups();

    public abstract MessagesDao messages();

    public abstract MessageStatsDao messageStats();

    public abstract EventsDao events();
}
