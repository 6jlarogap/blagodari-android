package org.blagodarie.repository.java;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Database (
        entities = {
                KeyType.class,
                User.class,
                Key.class,
                Like.class,
                UserKey.class,
                LikeKey.class,
                Contact.class,
                ContactKey.class,
                UserContact.class
        },
        version = 15)
abstract class AppDatabase
        extends RoomDatabase {

    private static final String DATABASE_NAME = "blag.db";

    private static volatile AppDatabase INSTANCE;

    AppDatabase () {
    }

    static AppDatabase getInstance (@NonNull final Context context) {
        synchronized (AppDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = buildDatabase(context);
            }
        }
        return INSTANCE;
    }

    private static AppDatabase buildDatabase (@NonNull final Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate (@NonNull final SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        prepopulateDatabase(context);
                    }
                })
                .addMigrations(MigrationKeeper.getMigrations())
                .fallbackToDestructiveMigration()
                .build();
    }

    private static void prepopulateDatabase (@NonNull final Context context) {
        final List<KeyType> keyTypes = new ArrayList<>();
        for (KeyType.Type t : KeyType.Type.values()) {
            keyTypes.add(t.getKeyType());
        }
        Executors.newSingleThreadExecutor().execute(() ->
                getInstance(context).getKeyTypeDao().insert(keyTypes)
        );
    }

    abstract ContactDao getContactDao ();

    abstract ContactKeyDao getContactKeyDao ();

    abstract UserContactDao getUserContactDao ();

    abstract KeyDao getKeyDao ();

    abstract KeyTypeDao getKeyTypeDao ();

    abstract LikeDao getLikeDao ();

    abstract LikeKeyDao getLikeKeyDao ();

    abstract UserDao getUserDao ();

    abstract UserKeyDao getUserKeyDao ();
}
