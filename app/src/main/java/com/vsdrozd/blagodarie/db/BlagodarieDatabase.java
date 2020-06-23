package com.vsdrozd.blagodarie.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vsdrozd.blagodarie.db.dao.ContactDao;
import com.vsdrozd.blagodarie.db.dao.ContactKeyzDao;
import com.vsdrozd.blagodarie.db.dao.UserContactDao;
import com.vsdrozd.blagodarie.db.dao.KeyzDao;
import com.vsdrozd.blagodarie.db.dao.KeyzTypeDao;
import com.vsdrozd.blagodarie.db.dao.LikeDao;
import com.vsdrozd.blagodarie.db.dao.LikeKeyzDao;
import com.vsdrozd.blagodarie.db.dao.UserDao;
import com.vsdrozd.blagodarie.db.dao.UserKeyzDao;
import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.ContactKeyz;
import com.vsdrozd.blagodarie.db.scheme.UserContact;
import com.vsdrozd.blagodarie.db.scheme.LikeKeyz;
import com.vsdrozd.blagodarie.db.scheme.Keyz;
import com.vsdrozd.blagodarie.db.scheme.KeyzType;
import com.vsdrozd.blagodarie.db.scheme.Like;
import com.vsdrozd.blagodarie.db.scheme.User;
import com.vsdrozd.blagodarie.db.scheme.UserKeyz;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
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
public abstract class BlagodarieDatabase extends RoomDatabase {

    private static BlagodarieDatabase INSTANCE;

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

    public static BlagodarieDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (BlagodarieDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private static BlagodarieDatabase buildDatabase(final Context appContext) {
        return Room.databaseBuilder(appContext, BlagodarieDatabase.class, DATABASE_NAME)
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate (@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);

                        Completable.fromAction(() -> {
                            List<KeyzType> keyzTypes = new ArrayList<>();
                            for (KeyzType.Types t : KeyzType.Types.values()) {
                                keyzTypes.add(t.createKeyzType());
                            }
                            BlagodarieDatabase.getInstance(appContext).getKeyzTypeDao().insertAndSetIds(keyzTypes);
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