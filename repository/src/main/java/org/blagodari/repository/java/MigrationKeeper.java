package org.blagodari.repository.java;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ex.diagnosticlib.Diagnostic;

final class MigrationKeeper {

    private MigrationKeeper () {
    }

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate (final SupportSQLiteDatabase database) {
            Diagnostic.i("Migrate from 5 to 6");
            database.execSQL("ALTER TABLE tbl_contact ADD COLUMN lookup_key VARCHAR");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate (final SupportSQLiteDatabase database) {
            Diagnostic.i("Migrate from 6 to 7");
            database.execSQL("ALTER TABLE tbl_contact_keyz ADD COLUMN is_actual INTEGER NOT NULL DEFAULT 1");
            database.execSQL("ALTER TABLE tbl_contact_keyz ADD COLUMN is_need_sync INTEGER NOT NULL DEFAULT 1");
            database.execSQL("PRAGMA foreign_keys=off");
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("CREATE TABLE IF NOT EXISTS `tbl_contact_new` (`Id` INTEGER PRIMARY KEY AUTOINCREMENT, `server_id` INTEGER, `owner_id` INTEGER NOT NULL, `lookup_key` TEXT NOT NULL, `name` TEXT NOT NULL, `photo_uri` TEXT, `likes_count` INTEGER NOT NULL, `fame` INTEGER NOT NULL, `sum_likes_count` INTEGER NOT NULL, `is_actual` INTEGER NOT NULL,  `is_need_sync` INTEGER NOT NULL, `update_timestamp` INTEGER NOT NULL, FOREIGN KEY(`owner_id`) REFERENCES `tbl_user`(`Id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
            database.execSQL("INSERT INTO tbl_contact_new SELECT id, server_id, owner_id, '', name, photo_uri, likes_count, fame, sum_likes_count, 1, 1, update_timestamp FROM tbl_contact");
            database.execSQL("DROP INDEX index_tbl_contact_owner_id");
            database.execSQL("DROP INDEX index_tbl_contact_server_id");
            database.execSQL("DROP TABLE tbl_contact");
            database.execSQL("ALTER TABLE tbl_contact_new RENAME TO tbl_contact");
            database.execSQL("CREATE  INDEX `index_tbl_contact_owner_id` ON `tbl_contact` (`owner_id`)");
            database.execSQL("CREATE UNIQUE INDEX `index_tbl_contact_server_id` ON `tbl_contact` (`server_id`)");
            database.execSQL("CREATE UNIQUE INDEX `index_tbl_contact_lookup_key` ON `tbl_contact` (`lookup_key`)");
            database.execSQL("COMMIT");
            database.execSQL("PRAGMA foreign_keys=on");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate (final SupportSQLiteDatabase database) {
            Diagnostic.i("Migrate from 7 to 8");
            database.execSQL("ALTER TABLE tbl_user ADD COLUMN sync_timestamp INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate (final SupportSQLiteDatabase database) {
            Diagnostic.i("Migrate from 8 to 9");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tbl_contact_name` ON `tbl_contact` (`name`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tbl_keyz_value` ON `tbl_keyz` (`value`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tbl_contact_likes_count` ON `tbl_contact` (`likes_count`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tbl_contact_fame` ON `tbl_contact` (`fame`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tbl_contact_sum_likes_count` ON `tbl_contact` (`sum_likes_count`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tbl_contact_update_timestamp` ON `tbl_contact` (`update_timestamp`)");
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate (final SupportSQLiteDatabase database) {
            Diagnostic.i("Migrate from 9 to 10");
            database.execSQL("ALTER TABLE tbl_like ADD COLUMN cancel_timestamp INTEGER");
            database.execSQL("ALTER TABLE tbl_like ADD COLUMN is_need_sync INTEGER NOT NULL DEFAULT 0");
            database.execSQL("DROP INDEX IF EXISTS index_tbl_like_owner_id_contact_id_timestamp");
        }
    };

    static Migration[] getMigrations () {
        return new Migration[]{
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10
        };
    }
}
