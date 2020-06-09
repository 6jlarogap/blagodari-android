package org.blagodari.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.blagodari.db.dao.ContactDao;
import org.blagodari.db.dao.ContactKeyzDao;
import org.blagodari.db.dao.UserContactDao;
import org.blagodari.db.dao.KeyzDao;
import org.blagodari.db.dao.KeyzTypeDao;
import org.blagodari.db.dao.LikeDao;
import org.blagodari.db.dao.LikeKeyzDao;
import org.blagodari.db.dao.UserDao;
import org.blagodari.db.dao.UserKeyzDao;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.ContactKeyz;
import org.blagodari.db.scheme.UserContact;
import org.blagodari.db.scheme.LikeKeyz;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;
import org.blagodari.db.scheme.Like;
import org.blagodari.db.scheme.User;
import org.blagodari.db.scheme.UserKeyz;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

/**
 * Абстрактный класс, представляющий БД.
 *
 * @author sergeGabrus
 */
@Database (
        entities = {
                KeyzType.class,
                User.class,
                Keyz.class,
                Like.class,
                UserKeyz.class,
                LikeKeyz.class,
                Contact.class,
                ContactKeyz.class,
                UserContact.class
        },
        version = 15)
public abstract class BlagodariDatabase extends RoomDatabase {

    private static BlagodariDatabase INSTANCE;

    private static final String DATABASE_NAME = "blag.db";

    public abstract ContactDao getContactDao ();

    public abstract ContactKeyzDao getContactKeyzDao ();

    public abstract UserContactDao getUserContactDao ();

    public abstract KeyzDao getKeyzDao ();

    public abstract KeyzTypeDao getKeyzTypeDao ();

    public abstract LikeDao getLikeDao ();

    public abstract LikeKeyzDao getLikeKeyzDao ();

    public abstract UserDao getUserDao ();

    public abstract UserKeyzDao getUserKeyzDao ();

    public static BlagodariDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (BlagodariDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private static BlagodariDatabase buildDatabase(final Context appContext) {
        return Room.databaseBuilder(appContext, BlagodariDatabase.class, DATABASE_NAME)
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate (@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);

                        Completable.fromAction(() -> {
                            List<KeyzType> keyzTypes = new ArrayList<>();
                            for (KeyzType.Types t : KeyzType.Types.values()) {
                                keyzTypes.add(t.createKeyzType());
                            }
                            BlagodariDatabase.getInstance(appContext).getKeyzTypeDao().insertAndSetIds(keyzTypes);
                        })
                                .subscribeOn(Schedulers.io())
                                .subscribe();
                    }
                })
                .addMigrations(MigrationKeeper.getMigrations())
                .fallbackToDestructiveMigration()
                .build();
    }

}