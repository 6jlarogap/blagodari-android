package org.blagodari.contacts;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.BuildConfig;
import org.blagodari.DataRepository;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.server.api.GetContactSumInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Класс синхронизирует контакты из адресной книги с БД.
 *
 * @author sergeGabrus
 */
public final class ContactSynchronizer {

    public interface ProgressListener {

        void onGetData (final int index, final int count);

        void onProcessingContacts (final int indexFrom, final int indexTo, final int size);

        void onDatabaseWrite (final int indexFrom, final int indexTo, final int size);

        void onContactRepositoryWrite (final int indexFrom, final int indexTo, final int size);

        void onFinish ();
    }

    private ContactSynchronizer () {
    }

    public synchronized static void syncByPage (
            @NonNull final Long userId,
            @NonNull final ContactRepository contactRepository,
            @NonNull final DataRepository dataRepository,
            @NonNull final ProgressListener progressListener,
            final int pageSize
    ) {
        if (BuildConfig.DEBUG) {
            Diagnostic.i("show all contacts before sync");
            //contactRepository.diagnosticContacts();
        }

        //получить список контактов из внутреннего репозитория (контакты ВР)
        final LongSparseArray<ContactWithKeyz> contentContactsWithKeyzByContactId = contactRepository.getAll(progressListener);
        final int contentContactsCount = contentContactsWithKeyzByContactId.size();
        Diagnostic.i("contentContacts size", contentContactsCount);

        //получить LongSparseArray контактов и ключей из БД
        final LongSparseArray<ContactWithKeyz> dbContacts = createDbContactWithKeyzLongSparseArray(userId, dataRepository);
        Diagnostic.i("dbContacts size", dbContacts.size());

        if (contentContactsCount > 0) {
            int indexFrom = 0;
            int indexTo = Math.min(pageSize, contentContactsCount);
            while (indexFrom != indexTo) {
                progressListener.onProcessingContacts(indexFrom + 1, indexTo, contentContactsCount);
                final LongSparseArray<ContactWithKeyz> subArray = getLongSparseSubArray(contentContactsWithKeyzByContactId, indexFrom, indexTo);

                //создать список контактов для записи идентификаторов
                final List<Pair<Long, Contact>> contactsForInsertId = new ArrayList<>();

                //создать список контактов с ключами для добавления в БД
                final List<ContactWithKeyz> contactsWithKeyzForInsert = new ArrayList<>();
                //создать список контактов с ключами для обновления в БД
                final List<ContactWithKeyz> contactsWithKeyzForUpdate = new ArrayList<>();

                //сгруппировать контакты
                groupContacts(
                        subArray,
                        dbContacts,
                        contactsWithKeyzForInsert,
                        contactsWithKeyzForUpdate,
                        contactsForInsertId
                );

                progressListener.onDatabaseWrite(indexFrom + 1, indexTo, contentContactsCount);
                //внести изменения в БД
                dataRepository.insertAndUpdateContacts(
                        userId,
                        contactsWithKeyzForInsert,
                        contactsWithKeyzForUpdate
                );

                final Collection<Long> contactIds = new ArrayList<>();
                for (int i = 0; i < subArray.size(); i++) {
                    contactIds.add(subArray.valueAt(i).getContact().getId());
                }
                Completable.
                        fromAction(() -> GetContactSumInfo.getInstance().execute(new GetContactSumInfo.DataIn(dataRepository, contactIds))).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe();

                progressListener.onContactRepositoryWrite(indexFrom + 1, indexTo, contentContactsCount);
                //записать идентификаторы во ВР
                writeIds(
                        contactsForInsertId,
                        contactRepository
                );

                indexFrom = indexTo;
                indexTo = Math.min(indexTo + pageSize, contentContactsCount);
            }
            //создать список контактов с ключами для удаления из БД
            final List<ContactWithKeyz> contactsWithKeyzForDelete = new ArrayList<>();
            for (int i = 0; i < dbContacts.size(); i++) {
                Diagnostic.i("contentContact", dbContacts.valueAt(i));
                Diagnostic.i("to delete");
                contactsWithKeyzForDelete.add(dbContacts.valueAt(i));
            }
            //внести изменения в БД
            dataRepository.removeContacts(
                    userId,
                    contactsWithKeyzForDelete
            );
        }

        if (BuildConfig.DEBUG) {
            Diagnostic.i("show all contacts after sync");
            //contactRepository.diagnosticContacts();
        }
        progressListener.onFinish();
    }

    private static <T> LongSparseArray<T> getLongSparseSubArray (
            @NonNull final LongSparseArray<T> longSparseArray,
            final int indexFrom,
            final int indexTo
    ) {
        final LongSparseArray<T> subArray = new LongSparseArray<>();
        for (int i = indexFrom; i < indexTo; i++) {
            subArray.put(longSparseArray.keyAt(i), longSparseArray.valueAt(i));
        }
        return subArray;
    }

    /**
     * Сравнивает и группирует контакты по спискам для вставки, обновления и удаления.
     *
     * @param contentContacts           Контакты из внутреннего репозитория.
     * @param dbContacts                Контакты из БД.
     * @param contactsWithKeyzForInsert Список контактов для вставки.
     * @param contactsWithKeyzForUpdate Список контактов для обновления.
     */
    @VisibleForTesting
    static void groupContacts (
            @NonNull final LongSparseArray<ContactWithKeyz> contentContacts,
            @NonNull final LongSparseArray<ContactWithKeyz> dbContacts,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForInsert,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForUpdate,
            @NonNull final List<Pair<Long, Contact>> contentContactListForInsertId
    ) {
        Diagnostic.i();
        Diagnostic.i("contentContacts size", contentContacts.size());
        for (int i = 0; i < contentContacts.size(); i++) {
            final ContactWithKeyz contentContactWithKeyz = contentContacts.valueAt(i);
            Diagnostic.i("contentContactWithKeyz", contentContactWithKeyz);

            //получить идентификатор контакта в БД из контакта адресной книги
            final Long blagodarieContactId = contentContactWithKeyz.getContact().getId();

            //если такой идентификатор существует
            if (blagodarieContactId != null) {

                //получить контакт с ключами из БД
                final ContactWithKeyz dbContactWithKeyz = dbContacts.get(blagodarieContactId);

                //если в нашей БД существует контакт с таким id
                if (dbContactWithKeyz != null) {
                    //если контакты не равны - добавить контакт в список для обновлений
                    if (!contentContactWithKeyz.equals(dbContactWithKeyz)) {
                        contactsWithKeyzForUpdate.add(contentContactWithKeyz);
                        Diagnostic.i("to update");
                    } else {
                        Diagnostic.i("no changes");
                    }
                    //удалить обработанный контакт с ключами из таблицы
                    dbContacts.remove(blagodarieContactId);
                } else {
                    //добавить контакт список для вставки
                    contactsWithKeyzForInsert.add(contentContactWithKeyz);
                    Diagnostic.i("to insert");
                }
            } else {
                //добавить контакт список для вставки
                contactsWithKeyzForInsert.add(contentContactWithKeyz);
                //добавить в список для записи идентификатора
                contentContactWithKeyz.getContact().setId(contentContactWithKeyz.getContact().getId());
                contentContactListForInsertId.add(new Pair<>(contentContacts.keyAt(i), contentContactWithKeyz.getContact()));

                Diagnostic.i("to insert and insert id");
            }
        }

        Diagnostic.i("contactsWithKeyzForInsert", contactsWithKeyzForInsert);
        Diagnostic.i("contactsWithKeyzForUpdate", contactsWithKeyzForUpdate);
    }

    private static void writeIds (
            @NonNull final List<Pair<Long, Contact>> contactsForInsertId,
            @NonNull final ContactRepository contactRepository
    ) {
        final int contactsPageSize = 50;
        if (contactsForInsertId.size() > 0) {
            int indexFrom = 0;
            int indexTo = Math.min(contactsPageSize, contactsForInsertId.size());
            while (indexFrom != indexTo) {
                contactRepository.insertBlagodarieContactIdsIntoContent(contactsForInsertId.subList(indexFrom, indexTo));
                indexFrom = indexTo;
                indexTo = Math.min(indexTo + contactsPageSize, contactsForInsertId.size());
            }
        }
    }

    private static LongSparseArray<ContactWithKeyz> createDbContactWithKeyzLongSparseArray (
            @NonNull final Long userId,
            @NonNull final DataRepository repository
    ) {
        final List<ContactWithKeyz> dbContactsWithKeyz = repository.getContactWithKeyzByUser(userId);
        final LongSparseArray<ContactWithKeyz> dbContactWithKeyzMap = new LongSparseArray<>();

        for (ContactWithKeyz contactWithKeyz : dbContactsWithKeyz) {
            dbContactWithKeyzMap.put(contactWithKeyz.getContact().getId(), contactWithKeyz);
        }

        return dbContactWithKeyzMap;
    }

}
