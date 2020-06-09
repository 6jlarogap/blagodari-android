package org.blagodari;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;

import com.ex.diagnosticlib.Diagnostic;

import org.blagodari.db.BlagodariDatabase;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.addent.KeyzWithContacts;
import org.blagodari.db.addent.LikeWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.ContactKeyz;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;
import org.blagodari.db.scheme.Like;
import org.blagodari.db.scheme.LikeKeyz;
import org.blagodari.db.scheme.User;
import org.blagodari.db.scheme.UserContact;
import org.blagodari.db.scheme.UserKeyz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Класс предоставляет API для работы с базой данных {@link BlagodariDatabase}.
 */
public final class DataRepository {

    /**
     * База данных.
     */
    private static BlagodariDatabase mDatabase;

    DataRepository (@NonNull final BlagodariDatabase database) {
        mDatabase = database;
    }

    /**
     * Получает из БД пользователя с заданным идентификатором, если пользователя с таким
     * идентификатором в БД не существует, то создает его и вставляет в БД.
     *
     * @param userId Идентификатор пользователя.
     * @return Объект {@link Single} для пользователя.
     */
    public final Single<User> getOrCreateUser (@NonNull final Long userId) {
        return Single.
                fromCallable(() -> {
                    User user = mDatabase.getUserDao().get(userId);
                    if (user == null) {
                        user = new User();
                        user.setId(userId);
                        mDatabase.getUserDao().insertAndSetId(user);
                    }
                    return user;
                });
    }

    /**
     * Проверяет авторизован ли пользователь.
     *
     * @param userId Идентификатор пользователя.
     * @return Объект {@link Single} для булева утверждения, авторизован ли пользователь.
     */
    public final Single<Boolean> isAuthorizedUser (@NonNull final Long userId) {
        return Single.fromCallable(() ->
                mDatabase.getKeyzDao().getCountByOwnerIdAndTypeId(userId, KeyzType.Types.GOOGLE_ACCOUNT_ID.getId()) > 0
        );
    }

    /**
     * Создает новый ключ и вставляет в БД.
     *
     * @param ownerId Владелец ключа.
     * @param value   Значение ключа.
     * @param typeId  Идентификатор типа ключа.
     * @return Созданный ключ.
     */
    public final Completable createKeyz (
            @Nullable final Long ownerId,
            @NonNull final String value,
            @NonNull final Long typeId
    ) {
        final Keyz keyz = new Keyz(value, typeId);
        keyz.setOwnerId(ownerId);
        return Completable.fromAction(() ->
                mDatabase.getKeyzDao().insertAndSetId(keyz)
        );
    }

    public final void removeContacts (
            @NonNull final Long userId,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForDelete
    ) {
        if (!contactsWithKeyzForDelete.isEmpty()) {
            mDatabase.runInTransaction(() -> {
                //удалить контакты
                if (!contactsWithKeyzForDelete.isEmpty()) {
                    deleteContacts(userId, contactsWithKeyzForDelete);
                }

                //пометить неопределенные ключи
                mDatabase.getKeyzDao().verifyVague();

                //пометить неопределенные связи благодарность-ключ
                mDatabase.getLikeKeyzDao().markVagueForVagueKeyz();

                //благодарности, имеющие неопределенные связи, отвязать от контактов
                mDatabase.getLikeDao().relateOffFromContactsForVagueKeyz();

                //пересчитать количество благодарностей у контактов
                mDatabase.getContactDao().calcLikeCount();
            });
        }
    }

    public final void insertAndUpdateContacts (
            @NonNull final Long userId,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForInsert,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForUpdate
    ) {
        if (!contactsWithKeyzForInsert.isEmpty() ||
                !contactsWithKeyzForUpdate.isEmpty()) {
            mDatabase.runInTransaction(() -> {
                //вставить контакты
                if (!contactsWithKeyzForInsert.isEmpty()) {
                    insertContactsForUser(userId, contactsWithKeyzForInsert);
                }

                //обновить контакты
                if (!contactsWithKeyzForUpdate.isEmpty()) {
                    updateContacts(userId, contactsWithKeyzForUpdate);
                }

                //пометить неопределенные ключи
                mDatabase.getKeyzDao().verifyVague();

                //пометить неопределенные связи благодарность-ключ
                mDatabase.getLikeKeyzDao().markVagueForVagueKeyz();

                //благодарности, имеющие неопределенные связи, отвязать от контактов
                mDatabase.getLikeDao().relateOffFromContactsForVagueKeyz();

                //пересчитать количество благодарностей у контактов
                mDatabase.getContactDao().calcLikeCount();
            });
        }
    }

    /**
     * Предусловие: контакты для обновления и удаления должны иметь идентификаторы.
     *
     * @param userId                    Идентификатор пользователя.
     * @param contactsWithKeyzForInsert Список контактов для вставки.
     * @param contactsWithKeyzForUpdate Список контактов для обновления.
     * @param contactsWithKeyzForDelete Список контактов для удаления.
     */
    public final void synchronizeContacts (
            @NonNull final Long userId,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForInsert,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForUpdate,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForDelete
    ) {
        if (!contactsWithKeyzForInsert.isEmpty() ||
                !contactsWithKeyzForUpdate.isEmpty() ||
                !contactsWithKeyzForDelete.isEmpty()) {
            mDatabase.runInTransaction(() -> {
                //удалить контакты
                if (!contactsWithKeyzForDelete.isEmpty()) {
                    deleteContacts(userId, contactsWithKeyzForDelete);
                }

                //вставить контакты
                if (!contactsWithKeyzForInsert.isEmpty()) {
                    insertContactsForUser(userId, contactsWithKeyzForInsert);
                }

                //обновить контакты
                if (!contactsWithKeyzForUpdate.isEmpty()) {
                    updateContacts(userId, contactsWithKeyzForUpdate);
                }

                //пометить неопределенные ключи
                mDatabase.getKeyzDao().verifyVague();

                //пометить неопределенные связи благодарность-ключ
                mDatabase.getLikeKeyzDao().markVagueForVagueKeyz();

                //благодарности, имеющие неопределенные связи, отвязать от контактов
                mDatabase.getLikeDao().relateOffFromContactsForVagueKeyz();

                //пересчитать количество благодарностей у контактов
                mDatabase.getContactDao().calcLikeCount();
            });
        }
    }

    /**
     * Обновляет контакты, устанавливает связи между контактами и их ключами, деактуализирует
     * связи между контактами и ключами, отныне им не принадлежащим.
     * Предусловие: все контакты должны иметь идентификаторы.
     *
     * @param contactsWithKeyzForUpdate Список контактов с ключами.
     */
    private void updateContacts (
            @NonNull final Long userId,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForUpdate
    ) {
        //извлечь список контактов
        final List<Contact> contactsForUpdate = new ArrayList<>(ContactWithKeyz.extractContactList(contactsWithKeyzForUpdate));
        //извлечь список ключей
        final List<Keyz> keyzForUpdate = new ArrayList<>(ContactWithKeyz.extractKeyzList(contactsWithKeyzForUpdate));
        //извлечь список идентификаторов контактов
        final List<Long> contactIdsForUpdate = new ArrayList<>(ContactWithKeyz.extractContactIds(contactsForUpdate));

        //обновить контакты
        mDatabase.getContactDao().update(contactsForUpdate);
        //вставить ключи
        mDatabase.getKeyzDao().insertAndSetIdsOrGetIdsFromDB(keyzForUpdate);
        //отвязать пользователя от всех ключей контактов
        mDatabase.getUserKeyzDao().markForDeleteByUserIdAndContactIds(userId, contactIdsForUpdate);
        //отвязать контакты от всех ключей
        mDatabase.getContactKeyzDao().deleteByContactIds(contactIdsForUpdate);
        //связать контакты с новыми ключами
        relateContactsToKeyz(contactsWithKeyzForUpdate);
        //извлечь список идентификаторов ключей
        final List<Long> keyzIdsForUpdate = new ArrayList<>(ContactWithKeyz.extractKeyzIds(keyzForUpdate));
        //привязать пользователя к новым ключам
        relateUserToKeyz(userId, keyzIdsForUpdate);
        //связи благодарностей для старых ключей пометить для удаления
        mDatabase.getLikeKeyzDao().markDeleteByContactIds(contactIdsForUpdate);
        //связать благодарности с новыми ключами
        relateLikesToKeyz(contactsWithKeyzForUpdate);
    }

    /**
     * Удаляет контакты из списка их связи с ключами, благодарностями и пользователем. Разрывает
     * связи между благодарностями и ключами.
     *
     * @param userId                    Идентификатор пользователя.
     * @param contactsWithKeyzForDelete Список контактов с ключами.
     */
    @VisibleForTesting
    void deleteContacts (
            @NonNull final Long userId,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzForDelete
    ) {
        //извлечь список контактов
        final List<Contact> contactsForDelete = new ArrayList<>(ContactWithKeyz.extractContactList(contactsWithKeyzForDelete));
        //извлечь список ключей
        final List<Keyz> keyzForDelete = new ArrayList<>(ContactWithKeyz.extractKeyzList(contactsWithKeyzForDelete));
        //извлечь список идентификаторов ключей
        final List<Long> keyzIdsForDelete = new ArrayList<>(ContactWithKeyz.extractKeyzIds(keyzForDelete));
        //извлечь список идентификаторов контактов
        final List<Long> contactIdsForDelete = new ArrayList<>(ContactWithKeyz.extractContactIds(contactsForDelete));

        //пометить связи благодарностей с ключами для удаления
        mDatabase.getLikeKeyzDao().markDeleted(contactIdsForDelete);
        //пометить благодарности для удаления
        mDatabase.getLikeDao().markForDeletedByContactIds(contactIdsForDelete);
        //разорвать связь между благодарностями и контактами
        mDatabase.getLikeDao().relateOffFromContacts(contactIdsForDelete);
        //удалить связи пользователя с ключами
        mDatabase.getUserKeyzDao().markForDelete(userId, keyzIdsForDelete);
        //удалить связи пользователя с контактами
        mDatabase.getUserContactDao().deleteByUserIdAndContactIds(userId, contactIdsForDelete);
        //удалить связи контактов с ключами
        mDatabase.getContactKeyzDao().deleteByContactIds(contactIdsForDelete);
        //удалить контакты
        mDatabase.getContactDao().deleteByContactIds(contactIdsForDelete);
    }

    /**
     * Вставляет новые контакты и ключи и связывает их с пользователем и существующими
     * благодарностями.
     *
     * @param userId               Идентификатор пользователя.
     * @param contactsWithKeyzList Список контактов с ключами.
     */
    @VisibleForTesting
    void insertContactsForUser (
            @NonNull final Long userId,
            @NonNull final List<ContactWithKeyz> contactsWithKeyzList
    ) {
        //извлечь список контактов
        List<Contact> contactsForInsert = new ArrayList<>(ContactWithKeyz.extractContactList(contactsWithKeyzList));

        //вставить контакты
        insertContacts(contactsForInsert);

        //извлечь список ключей
        List<Keyz> keyzForInsert = new ArrayList<>(ContactWithKeyz.extractKeyzList(contactsWithKeyzList));

        //вставить ключи
        insertKeyz(keyzForInsert);

        //связать контакты с ключами
        relateContactsToKeyz(contactsWithKeyzList);

        //связать пользователя с контактами
        relateUserToContacts(userId, contactsForInsert);

        //извлечь список идентификаторов ключей
        List<Long> keyzIdsForInsert = new ArrayList<>(ContactWithKeyz.extractKeyzIds(keyzForInsert));

        //связать пользователя с ключами
        relateUserToKeyz(userId, keyzIdsForInsert);
    }

    /**
     * Вставляет контакты из списка и проставляет им идентификаторы.
     *
     * @param contactList Список контактов.
     */
    @VisibleForTesting
    void insertContacts (@NonNull final List<Contact> contactList) {
        //Вставить контакты в БД
        mDatabase.getContactDao().insertAndSetIds(contactList);
    }

    /**
     * Создает связи {@link UserContact} между заданным пользователем и контактами из списка
     * и вставляет их в БД. Контакты должны иметь идентификаторы.
     *
     * @param userId   Идентификатор пользователя.
     * @param contacts Список контактов.
     */
    @VisibleForTesting
    void relateUserToContacts (
            @NonNull final Long userId,
            @NonNull final List<Contact> contacts
    ) {
        //создать список связей UserContact
        final List<UserContact> userContactsForInsert = new ArrayList<>();

        //Для всех контактов в списке контактов с ключами
        for (Contact contact : contacts) {
            //Контакт должен иметь идентификатор
            Diagnostic.Assert(
                    contact.getId() != null,
                    String.format(Locale.ENGLISH, "New contact %s did not receive identifier", contact)
            );
            //создать новую связь
            final UserContact userContact = new UserContact(userId, contact.getId());
            //вставить в список
            userContactsForInsert.add(userContact);
        }
        //вставить список связей в БД
        mDatabase.getUserContactDao().insertAndSetIds(userContactsForInsert);
    }

    /**
     * Вставляет в БД список ключей из списка и проставляем им идентификаторы.
     * Если ключ уже есть в БД, то проставляет ему идентификатор существующего в БД ключа.
     *
     * @param keyzList Список ключей.
     */
    public void insertKeyz (@NonNull final Collection<Keyz> keyzList) {
        mDatabase.getKeyzDao().insertAndSetIdsOrGetIdsFromDB(keyzList);
    }

    /**
     * Создает и вставляет в БД связи {@link ContactKeyz} между контактами и ключами.
     * Предусловие: контакты и ключи должны иметь идентификаторы.
     *
     * @param contactWithKeyzList Список контактов с ключами.
     */
    @VisibleForTesting
    void relateContactsToKeyz (@NonNull final List<ContactWithKeyz> contactWithKeyzList) {
        List<ContactKeyz> contactKeyzForInsert = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzList) {
            Diagnostic.Assert(
                    contactWithKeyz.getContact().getId() != null,
                    String.format(Locale.ENGLISH, "New contact %s did not receive identifier", contactWithKeyz.getContact())
            );
            for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
                Diagnostic.Assert(
                        keyz.getId() != null,
                        String.format(Locale.ENGLISH, "Keyz %s did not receive identifier", keyz)
                );
                contactKeyzForInsert.add(new ContactKeyz(contactWithKeyz.getContact().getId(), keyz.getId()));
            }
        }
        mDatabase.getContactKeyzDao().insertAndSetIds(contactKeyzForInsert);
    }

    void relateContactToKeyz (@NonNull final ContactWithKeyz contactWithKeyz) {
        final List<ContactKeyz> contactKeyzForInsert = new ArrayList<>();
        Diagnostic.Assert(
                contactWithKeyz.getContact().getId() != null,
                String.format(Locale.ENGLISH, "New contact %s did not receive identifier", contactWithKeyz.getContact())
        );
        for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
            Diagnostic.Assert(
                    keyz.getId() != null,
                    String.format(Locale.ENGLISH, "Keyz %s did not receive identifier", keyz)
            );
            contactKeyzForInsert.add(new ContactKeyz(contactWithKeyz.getContact().getId(), keyz.getId()));
        }
        mDatabase.getContactKeyzDao().insertAndSetIds(contactKeyzForInsert);
    }

    /**
     * Связывает пользователя с ключами.
     *
     * @param userId  Идентификатор пользователя.
     * @param keyzIds Список идентификаторов ключей.
     */
    @VisibleForTesting
    void relateUserToKeyz (
            @NonNull final Long userId,
            @NonNull final List<Long> keyzIds
    ) {
        //создать список связей UserKeyz
        List<UserKeyz> userKeyzForInsert = new ArrayList<>();
        for (Long keyzId : keyzIds) {
            assert keyzId != null;
            userKeyzForInsert.add(new UserKeyz(userId, keyzId));
        }
        //вставить список в БД
        mDatabase.getUserKeyzDao().insertAndSetIds(userKeyzForInsert);
    }

    @VisibleForTesting
    void relateLikesToKeyz (@NonNull final List<ContactWithKeyz> contactWithKeyzList) {
        final List<LikeKeyz> likeKeyzListForInsert = new ArrayList<>();
        final List<LikeKeyz> likeKeyzListUpdate = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzList) {
            final long contactId = contactWithKeyz.getContact().getId();
            final List<Like> likeList = mDatabase.getLikeDao().getByContactId(contactId);
            for (Like like : likeList) {
                for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
                    LikeKeyz likeKeyz = mDatabase.getLikeKeyzDao().getByLikeIdAndKeyzId(like.getId(), keyz.getId());
                    if (likeKeyz == null) {
                        likeKeyzListForInsert.add(new LikeKeyz(like.getId(), keyz.getId()));
                    } else {
                        likeKeyz.setDeleted(false);
                        likeKeyzListUpdate.add(likeKeyz);
                    }
                }
            }
        }
        mDatabase.getLikeKeyzDao().insertAndSetIds(likeKeyzListForInsert);
        mDatabase.getLikeKeyzDao().update(likeKeyzListUpdate);
    }

    @VisibleForTesting
    void relateLikesToKeyz (@NonNull final ContactWithKeyz contactWithKeyz) {
        final List<LikeKeyz> likeKeyzListForInsert = new ArrayList<>();
        final List<LikeKeyz> likeKeyzListUpdate = new ArrayList<>();
        final long contactId = contactWithKeyz.getContact().getId();
        final List<Like> likeList = mDatabase.getLikeDao().getByContactId(contactId);
        for (Like like : likeList) {
            for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
                LikeKeyz likeKeyz = mDatabase.getLikeKeyzDao().getByLikeIdAndKeyzId(like.getId(), keyz.getId());
                if (likeKeyz == null) {
                    likeKeyzListForInsert.add(new LikeKeyz(like.getId(), keyz.getId()));
                } else {
                    likeKeyz.setDeleted(false);
                    likeKeyzListUpdate.add(likeKeyz);
                }
            }
        }
        mDatabase.getLikeKeyzDao().insertAndSetIds(likeKeyzListForInsert);
        mDatabase.getLikeKeyzDao().update(likeKeyzListUpdate);
    }

    public final List<ContactWithKeyz> getContactWithKeyzByUser (@NonNull final Long userId) {
        return mDatabase.getContactDao().getContactsWithKeyzByUser(userId);
    }

    public final ContactWithKeyz getContactWithKeyzByContactId (@NonNull final Long contactId) {
        return mDatabase.getContactDao().getContactWithKeyz(contactId);
    }

    public final List<ContactWithKeyz> getContactsWithKeyzByContactIds (@NonNull final Collection<Long> contactIds) {
        return mDatabase.getContactDao().getContactsWithKeyz(contactIds);
    }

    public final DataSource.Factory<Integer, Contact> getContactsByUserOrderByName (
            @NonNull final Long userId,
            @NonNull final String filter
    ) {
        Diagnostic.i();
        return mDatabase.getContactDao().getByUserOrderByName(userId, filter);
    }

    public final DataSource.Factory<Integer, Contact> getContactsByUserOrderByFame (
            @NonNull final Long userId,
            @NonNull final String filter
    ) {
        Diagnostic.i();
        return mDatabase.getContactDao().getByUserOrderByFame(userId, filter);
    }

    public final DataSource.Factory<Integer, Contact> getContactsByUserOrderByLikeCount (
            @NonNull final Long userId,
            @NonNull final String filter
    ) {
        Diagnostic.i();
        return mDatabase.getContactDao().getByUserOrderByLikeCount(userId, filter);
    }

    public final DataSource.Factory<Integer, Contact> getContactsByUserOrderBySumLikeCount (
            @NonNull final Long userId,
            @NonNull final String filter
    ) {
        Diagnostic.i();
        return mDatabase.getContactDao().getByUserOrderBySumLikeCount(userId, filter);
    }

    public final DataSource.Factory<Integer, Contact> getContactsByUserOrderByTime (
            @NonNull final Long userId,
            @NonNull final String filter
    ) {
        Diagnostic.i();
        return mDatabase.getContactDao().getByUserOrderByTime(userId, filter);
    }

    public void determineLikeContactId () {
        final Collection<LikeWithKeyz> likeWithEmptyContactId = mDatabase.getLikeDao().getWithEmptyContactId();
        for (LikeWithKeyz likeWithKeyz : likeWithEmptyContactId) {
            mDatabase.runInTransaction(() -> {
                Long contactId = findContactId(likeWithKeyz.getKeyzSet());
                if (contactId != null) {
                    likeWithKeyz.getLike().setContactId(contactId);
                    mDatabase.getLikeDao().update(likeWithKeyz.getLike());
                }
            });
        }
    }

    @Nullable
    public Long findContactId (@NonNull final Collection<Keyz> keyzList) {
        final Set<Contact> contacts = new HashSet<>();
        for (Keyz keyz : keyzList) {
            if (keyz != null) {
                final KeyzWithContacts keyzWithContacts = getKeyzWithContactsByKeyzId(keyz.getId());
                if (keyzWithContacts != null && !keyzWithContacts.getKeyz().getVague()) {
                    if (keyzWithContacts.getContactList().size() == 1) {
                        contacts.add(keyzWithContacts.getContactList().get(0));
                    }
                }
            }
        }
        Long contactId = null;
        if (contacts.size() == 1) {
            contactId = contacts.iterator().next().getId();
        }
        return contactId;
    }

    public synchronized void createLikeForContact (
            @NonNull final Long ownerId,
            @NonNull final Long contactId,
            final long createTimestamp
    ) {
        Diagnostic.i();
        mDatabase.runInTransaction(() -> {
            //Создать благодарность
            final Like like = new Like(ownerId, createTimestamp);
            like.setContactId(contactId);
            // Вставить благодарность
            mDatabase.getLikeDao().insertAndSetId(like);

            //Получить список связей контакт-ключ для заданного контакта
            final Collection<ContactKeyz> contactKeyzList = mDatabase.getContactKeyzDao().getByContactId(contactId);

            //Создать список связей благодарность-ключ
            final Collection<LikeKeyz> likeKeyzList = new ArrayList<>();
            for (ContactKeyz contactKeyz : contactKeyzList) {
                likeKeyzList.add(new LikeKeyz(like.getId(), contactKeyz.getKeyzId()));
            }
            //Вставить связи благодарность-ключ
            mDatabase.getLikeKeyzDao().insertAndSetIds(likeKeyzList);

            //Увеличить количество благодарностей контакта
            mDatabase.getContactDao().incrementContactLikeCount(contactId);
            mDatabase.getContactDao().incrementContactSumLikeCount(contactId);
        });
    }

    public final Completable cancelLike (@NonNull final Like like) {
        like.setCancelTimestamp(System.currentTimeMillis());
        return Completable.fromAction(() -> mDatabase.runInTransaction(() -> cancelLike(like, like.getContactId())));
    }

    private void cancelLike (
            @NonNull final Like like,
            @NonNull final Long contactId
    ) {
        //Отменить благодарность
        mDatabase.getLikeDao().update(like);
        //Уменьшить количество благодарностей контакта
        mDatabase.getContactDao().decrementContactLikeCount(contactId);
        mDatabase.getContactDao().decrementContactSumLikeCount(contactId);
    }

    public final User getUser (@NonNull final Long userId) {
        return mDatabase.getUserDao().get(userId);
    }

    public final List<Keyz> getKeyzByOwnerIdAndTypeId (
            @NonNull final Long ownerId,
            @NonNull final Long typeId
    ) {
        return mDatabase.getKeyzDao().getByOwnerIdAndTypeId(ownerId, typeId);
    }

    public final void updateUser (@NonNull final User user) {
        mDatabase.getUserDao().update(user);
    }

    public final void updateKeyz (@NonNull final Keyz keyz) {
        mDatabase.getKeyzDao().update(keyz);
    }

    public final void updateKeyz (@NonNull final List<Keyz> keyzList) {
        mDatabase.getKeyzDao().update(keyzList);
    }

    public final void updateUserKeyz (@NonNull final List<UserKeyz> userKeyzList) {
        mDatabase.getUserKeyzDao().update(userKeyzList);
    }

    public final LiveData<Boolean> isAuthorizedNotSyncedUser (@NonNull final Long userId) {
        return mDatabase.getUserDao().isAuthorizedNotSynced(userId);
    }

    public final LiveData<Boolean> isExistsKeyzForGetOrCreate (@NonNull final Long userId) {
        return mDatabase.getKeyzDao().isExistsForGetOrCreate(userId);
    }

    public final List<Keyz> getKeyzForGetOrCreate (@NonNull final Long userId) {
        return mDatabase.getKeyzDao().getForGetOrCreate(userId);
    }

    public final List<UserKeyz> getUserKeyzForGetOrCreate (@NonNull final Long userId) {
        return mDatabase.getUserKeyzDao().getForGetOrCreate(userId);
    }

    public final Long getUserServerId (@NonNull final Long userId) {
        return mDatabase.getUserDao().getServerId(userId);
    }

    public final List<Like> getLikesForAddLike (@NonNull final Long ownerId) {
        return mDatabase.getLikeDao().getForAdd(ownerId);
    }

    public final List<LikeKeyz> getLikeKeyzForAddLike (@NonNull final Long ownerId) {
        return mDatabase.getLikeKeyzDao().getForAddLike(ownerId);
    }

    public final void updateLike (@NonNull final List<Like> likeList) {
        mDatabase.getLikeDao().update(likeList);
    }

    public final void updateLikeKeyz (@NonNull final List<LikeKeyz> likeKeyzList) {
        mDatabase.getLikeKeyzDao().update(likeKeyzList);
    }

    public final LiveData<Boolean> isExistsLikeForAdd (@NonNull final Long ownerId) {
        return mDatabase.getLikeDao().isExistsForAdd(ownerId);
    }

    public final Long getKeyzServerId (@NonNull final Long keyzId) {
        return mDatabase.getKeyzDao().getServerId(keyzId);
    }

    public final List<UserKeyz> getUserKeyzForDelete (@NonNull final Long userId) {
        return mDatabase.getUserKeyzDao().getForDelete(userId);
    }

    public final LiveData<Boolean> isExistsUserKeyzForDelete (@NonNull final Long userId) {
        return mDatabase.getUserKeyzDao().isExistsForDelete(userId);
    }

    public final List<LikeKeyz> getLikeKeyzForGetOrCreate (@NonNull final Long userId) {
        return mDatabase.getLikeKeyzDao().getForGetOrCreate(userId);
    }

    public final Long getLikeServerId (@NonNull final Long likeId) {
        return mDatabase.getLikeDao().getServerId(likeId);
    }

    public final LiveData<Boolean> isExistsLikeKeyzForGetOrCreate (@NonNull final Long userId) {
        return mDatabase.getLikeKeyzDao().isExistsForGetOrCreate(userId);
    }

    public final List<Like> getLikeForCancelLike (@NonNull final Long userId) {
        return mDatabase.getLikeDao().getForCancelLike(userId);
    }

    public final LiveData<Boolean> isExistsLikeForCancel (@NonNull final Long userId) {
        return mDatabase.getLikeDao().isExistsForCancelLike(userId);
    }

    public final Boolean isUserSynced (@NonNull final Long userId) {
        return mDatabase.getUserDao().isSynced(userId);
    }

    public final void insertLikes (@NonNull final List<Like> likeList) {
        mDatabase.getLikeDao().insertAndSetIds(likeList);
    }

    public final void insertLike (@NonNull final Like like) {
        mDatabase.getLikeDao().insertAndSetId(like);
    }

    public final Long getLikeIdByServerId (@NonNull final Long likeServerId) {
        return mDatabase.getLikeDao().getIdByServerId(likeServerId);
    }

    public final Long getKeyzIdByServerId (@NonNull final Long keyzServerId) {
        return mDatabase.getKeyzDao().getIdByServerId(keyzServerId);
    }

    public final void insertLikeKeyz (@NonNull final List<LikeKeyz> likeKeyzList) {
        mDatabase.getLikeKeyzDao().insertAndSetIds(likeKeyzList);
    }

    public final void updateContact (@NonNull final Collection<Contact> contactList) {
        mDatabase.getContactDao().update(contactList);
    }

    public final List<Like> getLikeForDelete (@NonNull final Long ownerId) {
        return mDatabase.getLikeDao().getForDelete(ownerId);
    }

    public final void deleteLikes (@NonNull final List<Like> likeList) {
        mDatabase.getLikeDao().delete(likeList);
    }

    public final void removeLike (@NonNull final Like like) {
        List<LikeKeyz> likeKeyzList = mDatabase.getLikeKeyzDao().getByLikeId(like.getId());
        for (LikeKeyz likeKeyz : likeKeyzList) {
            likeKeyz.setDeleted(true);
        }
        mDatabase.getLikeKeyzDao().update(likeKeyzList);
        like.setDeleted(true);
        mDatabase.getLikeDao().update(like);
    }

    public final LiveData<Boolean> isExistsLikeForDelete (@NonNull final Long ownerId) {
        return mDatabase.getLikeDao().isExistsForDelete(ownerId);
    }

    public final LiveData<Boolean> isExistsLikeKeyzForDelete (@NonNull final Long ownerId) {
        return mDatabase.getLikeKeyzDao().isExistsForDelete(ownerId);
    }

    public final List<LikeKeyz> getLikeKeyzForDelete (@NonNull final Long userId) {
        return mDatabase.getLikeKeyzDao().getForDelete(userId);
    }

    public final void deleteLikeKeyz (@NonNull final List<LikeKeyz> likeKeyzList) {
        mDatabase.getLikeKeyzDao().delete(likeKeyzList);
    }

    public final LiveData<Contact> getContactLiveData (@NonNull final Long contactId) {
        return mDatabase.getContactDao().getLiveData(contactId);
    }

    public final LiveData<List<Keyz>> getKeyzLiveDataByContactIdAndTypeId (
            @NonNull final Long contactId,
            @NonNull final Long typeId
    ) {
        return mDatabase.getKeyzDao().getLiveDataByContactIdAndTypeId(contactId, typeId);
    }

    public final LiveData<List<Like>> getLikeLiveDataByContactIdAndOwnerId (
            @NonNull final Long contactId,
            @NonNull final Long ownerId
    ) {
        return mDatabase.getLikeDao().getLiveDataByContactIdAndOwnerId(contactId, ownerId);
    }

    public final LiveData<List<KeyzWithContacts>> getVagueKeyzWithContacts () {
        return mDatabase.getKeyzDao().getVagueKeyzWithContacts();
    }

    public final LiveData<List<LikeWithKeyz>> getVagueLikeWithKeyz () {
        return mDatabase.getLikeDao().getVagueLikeWithKeyz();
    }

    public final LiveData<List<LikeWithKeyz>> getLikeWithMissingKeyz () {
        return mDatabase.getLikeDao().getLikeWithMissingKeyz();
    }

    public final LiveData<List<ContactWithKeyz>> getNamesakeContactsWithKeyz () {
        return mDatabase.getContactDao().getNamesakeContactsWithKeyz();
    }

    public final KeyzWithContacts getKeyzWithContactsByKeyzId (@NonNull final Long keyzId) {
        return mDatabase.getKeyzDao().getKeyzWithContactsByKeyzId(keyzId);
    }

    public final Keyz getKeyzByValueAndTypeId (
            @NonNull final String value,
            @NonNull final Long typeId
    ) {
        return mDatabase.getKeyzDao().getByValueAndTypeId(value, typeId);
    }

    public final LiveData<List<Contact>> getContactsByUserId (@NonNull final Long userId) {
        return mDatabase.getContactDao().getByUserId(userId);
    }

    public final void relateLikeToContact (
            @NonNull final Like like,
            @NonNull final Long contactId
    ) {
        mDatabase.runInTransaction(() -> {
            final List<LikeKeyz> likeKeyzList = mDatabase.getLikeKeyzDao().getByLikeId(like.getId());
            for (LikeKeyz likeKeyz : likeKeyzList) {
                likeKeyz.setDeleted(true);
            }
            mDatabase.getLikeKeyzDao().update(likeKeyzList);

            final List<Keyz> keyzList = mDatabase.getKeyzDao().getKeyzByContactId(contactId);
            final List<LikeKeyz> newLikeKeyz = new ArrayList<>();
            for (Keyz keyz : keyzList) {
                final LikeKeyz likeKeyz = new LikeKeyz(like.getId(), keyz.getId());
                likeKeyz.setVague(keyz.getVague());
                newLikeKeyz.add(likeKeyz);
            }
            mDatabase.getLikeKeyzDao().insertAndSetIds(newLikeKeyz);

            like.setContactId(contactId);
            mDatabase.getLikeDao().update(like);

            mDatabase.getContactDao().calcLikeCount(contactId);
        });
    }

    public final void calcLikeCount () {
        mDatabase.getContactDao().calcLikeCount();
    }

    public final void addContactWithKeyz (
            @NonNull final Long userId,
            @NonNull final ContactWithKeyz contactWithKeyz
    ) {
        mDatabase.runInTransaction(() -> {
            //вставить контакт
            mDatabase.getContactDao().insertAndSetId(contactWithKeyz.getContact());
            //связать пользователя с контактом
            final UserContact userContact = new UserContact(userId, contactWithKeyz.getContact().getId());
            mDatabase.getUserContactDao().insertAndSetId(userContact);
            //вставить ключи
            mDatabase.getKeyzDao().insertAndSetIdsOrGetIdsFromDB(contactWithKeyz.getKeyzSet());
            //связать контакт с ключами
            for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
                final ContactKeyz contactKeyz = new ContactKeyz(contactWithKeyz.getContact().getId(), keyz.getId());
                mDatabase.getContactKeyzDao().insertAndSetId(contactKeyz);
            }
            //связать пользователя с ключами
            for (Keyz keyz : contactWithKeyz.getKeyzSet()) {
                final UserKeyz userKeyz = new UserKeyz(userId, keyz.getId());
                mDatabase.getUserKeyzDao().insertAndSetId(userKeyz);
            }

            commonOperations();
        });
    }

    public final void updateContactWithKeyz (
            @NonNull final Long userId,
            @NonNull final ContactWithKeyz contactWithKeyz
    ) {
        mDatabase.runInTransaction(() -> {
            //обновить контакт
            mDatabase.getContactDao().update(contactWithKeyz.getContact());
            //вставить ключи
            mDatabase.getKeyzDao().insertAndSetIdsOrGetIdsFromDB(contactWithKeyz.getKeyzSet());
            //отвязать пользователя от всех ключей контактов
            mDatabase.getUserKeyzDao().markForDeleteByUserIdAndContactId(userId, contactWithKeyz.getContact().getId());
            //отвязать контакты от всех ключей
            mDatabase.getContactKeyzDao().deleteByContactId(contactWithKeyz.getContact().getId());
            //связать контакты с новыми ключами
            relateContactToKeyz(contactWithKeyz);
            //извлечь список идентификаторов ключей
            final List<Long> keyzIdsForUpdate = new ArrayList<>(ContactWithKeyz.extractKeyzIds(contactWithKeyz.getKeyzSet()));
            //привязать пользователя к новым ключам
            relateUserToKeyz(userId, keyzIdsForUpdate);
            //связи благодарностей для старых ключей пометить для удаления
            mDatabase.getLikeKeyzDao().markDeleteByContactId(contactWithKeyz.getContact().getId());
            //связать благодарности с новыми ключами
            relateLikesToKeyz(contactWithKeyz);

            commonOperations();
        });
    }

    public final void deleteContactWithKeyz (
            @NonNull final Long userId,
            @NonNull final List<ContactWithKeyz> contactWithKeyz
    ) {
        mDatabase.runInTransaction(() -> {
            deleteContacts(userId, contactWithKeyz);

            commonOperations();
        });
    }

    private void commonOperations () {
        //пометить неопределенные ключи
        mDatabase.getKeyzDao().verifyVague();

        //пометить неопределенные связи благодарность-ключ
        mDatabase.getLikeKeyzDao().markVagueForVagueKeyz();

        //благодарности, имеющие неопределенные связи, отвязать от контактов
        mDatabase.getLikeDao().relateOffFromContactsForVagueKeyz();

        //пересчитать количество благодарностей у контактов
        mDatabase.getContactDao().calcLikeCount();
    }

    public final Boolean isHaveNamesake (@NonNull final Long contactId) {
        return mDatabase.getContactDao().isHaveNamesake(contactId);
    }
}
