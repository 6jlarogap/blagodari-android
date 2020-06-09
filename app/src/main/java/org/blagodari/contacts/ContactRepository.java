package org.blagodari.contacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.AccountGeneral;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Класс для создания объектов сущностей БД из контактов внутреннего репозитория.
 *
 * @author sergeGabrus
 */
public final class ContactRepository {

    /**
     * MIME-тип идентификатора контакта в БД.
     */
    private static final String BLAGODARIE_CONTACT_ID_MIME_TYPE = "vnd.android.cursor.item/vnd.org.blagodarie.contact_id";

    /**
     * Поле в таблице {@link ContactsContract.Data} для идентификатора контакта.
     */
    private static final String BLAGODARIE_ID = ContactsContract.Data.DATA1;

    /**
     * Поставщик контента.
     */
    @NonNull
    private final ContentResolver mContentResolver;

    /**
     * Менеджер аккаунтов.
     */
    @NonNull
    private final AccountManager mAccountManager;

    /**
     * Аккаунт.
     */
    @NonNull
    private final Account mAccount;

    public ContactRepository (
            @NonNull final ContentResolver contentResolver,
            @NonNull final AccountManager accountManager,
            @NonNull final Account account
    ) {
        this.mContentResolver = contentResolver;
        this.mAccountManager = accountManager;
        this.mAccount = account;
    }

    final LongSparseArray<ContactWithKeyz> getAll (@NonNull final ContactSynchronizer.ProgressListener progressListener) {
        Diagnostic.i();
        //Создать результирующий список контактов
        final LongSparseArray<ContactWithKeyz> contentContactsByContactId = new LongSparseArray<>();
        //Получить курсор контактов
        final Cursor contactsCursor = getContactsCursor();
        //Если курсор не пустой
        if (contactsCursor != null) {
            Diagnostic.i("contacts count", contactsCursor.getCount());
            int contactIndex = 1;
            //Пока в курсоре есть записи
            while (contactsCursor.moveToNext()) {
                progressListener.onGetData(contactIndex, contactsCursor.getCount());
                final long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                //Попытаться создать контакт с ключами
                final ContactWithKeyz contactWithKeyz = attemptCreateContactWithKeyz(contactId, contactsCursor);
                //если удалось создать
                if (contactWithKeyz != null) {
                    //поместить в результирующий список
                    contentContactsByContactId.put(contactId, contactWithKeyz);
                }
                contactIndex++;
            }
            //Закрыть курсор
            contactsCursor.close();
        }
        //Вернуть результат
        return contentContactsByContactId;
    }

    @NonNull
    private Set<Integer> getRawContactIds (@NonNull final Long contactId) {
        final Set<Integer> rawContactIds = new HashSet<>();
        final Cursor rawContactsCursor = getRawContactsCursor(contactId);
        if (rawContactsCursor != null) {
            if (rawContactsCursor.getCount() > 0) {
                while (rawContactsCursor.moveToNext()) {
                    final int rawContactId = rawContactsCursor.getInt(rawContactsCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                    rawContactIds.add(rawContactId);
                }
            }
            rawContactsCursor.close();
        }
        Diagnostic.i("rawContactIds", rawContactIds);
        return rawContactIds;
    }

    final void insertBlagodarieContactIdsIntoContent (
            @NonNull final Collection<Pair<Long, Contact>> contactIdWithContactCollection
    ) {
        Diagnostic.i();
        createAccount();
        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int i = 0;
        for (Pair<Long, Contact> contactIdWithcontact : contactIdWithContactCollection) {
            final Long contactId = contactIdWithcontact.first;
            final Long blagodarieId = contactIdWithcontact.second != null ? contactIdWithcontact.second.getId() : null;
            Diagnostic.i("contact content id", contactId);
            Diagnostic.i("contact blagodarie id", blagodarieId);
            if (contactId != null && blagodarieId != null) {
                //вставляем raw_contact
                ContentProviderOperation.Builder op =
                        ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, AccountGeneral.ACCOUNT_NAME);
                ops.add(op.build());

                //получаем идентификаторы raw_contacts, привязанные к контакту
                final Set<Integer> rawContactIds = getRawContactIds(contactId);
                //явно агрегируем со всеми raw_contacts, привязанными к контакту
                for (Integer rawContactId : rawContactIds) {
                    op = ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                            .withValue(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
                            .withValueBackReference(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, i)
                            .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, rawContactId);
                    ops.add(op.build());
                }

                /*необходимо добавить строку с именем (можно даже с пустым), иначе при изменении контакта,
                 наш raw_contact отвязывается от контакта и наш идентификатор невозможно найти*/
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, i)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, null);
                ops.add(op.build());

                //вставляем наш идентификатор
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, i)
                        .withValue(ContactsContract.Data.MIMETYPE, BLAGODARIE_CONTACT_ID_MIME_TYPE)
                        .withValue(BLAGODARIE_ID, blagodarieId);
                op.withYieldAllowed(true);
                ops.add(op.build());
                i = ops.size();
            }
        }

        try {
            this.mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Diagnostic.e(e);
        }
    }

    private void createAccount () {
        this.mAccountManager.addAccountExplicitly(this.mAccount, null, null);
    }

    /**
     * Пытается создать контакт с ключами из текущей строки курсора.
     * Предусловие: курсор должен быть открыт.
     *
     * @param contactsCursor Курсор по контактам.
     * @return Контакт с ключами.
     */
    private ContactWithKeyz attemptCreateContactWithKeyz (
            @NonNull final Long contactId,
            @NonNull final Cursor contactsCursor
    ) {
        Diagnostic.i();
        ContactWithKeyz contactWithKeyz = null;
        //считать имя
        final String title = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        Diagnostic.i("title", title);
        //контакты с пустым именем игнорируем (по идее таких быть и не должно, но на всякий случай предохраняемся)
        if (title != null && !title.isEmpty()) {
            final String photoUri = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
            final Long blagodarieContactId = getContactBlagodarieId(contactId);

            Diagnostic.i("contentContactId", contactId);
            Diagnostic.i("photoUri", photoUri);
            Diagnostic.i("blagodarieContactId", blagodarieContactId);

            //создать контакт
            final Contact contact = new Contact(title);
            contact.setId(blagodarieContactId);
            contact.setPhotoUri(photoUri);

            //создать список ключей
            final Set<Keyz> keyzList = new HashSet<>();
            //добавить в список ключей телефоны
            keyzList.addAll(getPhones(contactId));
            //добавить в список ключей емэйлы
            keyzList.addAll(getEmails(contactId));
            Diagnostic.i("keyzList", keyzList);

            contactWithKeyz = new ContactWithKeyz(contact, keyzList);
            Diagnostic.i("contactWithKeyz", contactWithKeyz);
        }
        return contactWithKeyz;
    }

    /**
     * Возвращает идентификатор контакта в БД для заданного контакта.
     *
     * @param contactContentId Идентификатор контакта в центральном репозитории.
     * @return Идентификатор контакта в БД.
     */
    private Long getContactBlagodarieId (@NonNull final Long contactContentId) {
        Diagnostic.i("contactContentId", contactContentId);
        Long contactBlagodarieId = null;
        final Cursor blagodarieIdCursor = getBlagodarieIdCursor(contactContentId);
        if (blagodarieIdCursor != null) {
            if (blagodarieIdCursor.getCount() > 0) {
                //идентификатор в центральном репозитории должен быть один
                if (blagodarieIdCursor.getCount() != 1) {
                    throw new AssertionError(String.format(Locale.ENGLISH, "Contact %d have more than one blagodarie contact id", contactContentId));
                }
                blagodarieIdCursor.moveToFirst();
                contactBlagodarieId = blagodarieIdCursor.getLong(blagodarieIdCursor.getColumnIndex(BLAGODARIE_ID));
                Diagnostic.i("contactBlagodarieId", contactBlagodarieId);
            } else {
                Diagnostic.i("empty cursor");
            }
            blagodarieIdCursor.close();
        } else {
            Diagnostic.i("cursor is null");
        }
        return contactBlagodarieId;
    }

    /**
     * Возвращает курсор для идентификатора контакта в БД для заданного контакта.
     *
     * @param contactContentId Идентификатор контакта в центральном репозитории.
     * @return Курсор для идентификатора контакта в БД
     */
    private Cursor getBlagodarieIdCursor (@NonNull final Long contactContentId) {
        final Uri uri = ContactsContract.Data.CONTENT_URI;
        final String[] projection = new String[]{BLAGODARIE_ID};
        final String selection = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        final String[] selectionArgs = new String[]{contactContentId.toString(), BLAGODARIE_CONTACT_ID_MIME_TYPE};

        return getCursor(
                uri,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    /**
     * Возвращает курсор для всех видимых контактов ({@code IN_VISIBLE_GROUP = 1}) из центрального репозитория.
     *
     * @return Курсор для всех видимых контактов.
     */
    private Cursor getContactsCursor () {
        final Uri uri = ContactsContract.Contacts.CONTENT_URI;
        final String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
        };
        final String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ?";
        final String[] selectionArgs = new String[]{"1"};
        final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " ASC";

        return getCursor(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    /**
     * Создает и возвращает список телефонных номеров для заданного контакта.
     *
     * @param contactContentId Идентификатор контакта в центральном репозитории.
     * @return Список телефонных номеров.
     * @throws SecurityException Генерируется если доступ к чтению контактов запрещен.
     */
    private Set<Keyz> getPhones (@NonNull final Long contactContentId) throws SecurityException {
        Diagnostic.i("contactId", contactContentId);
        final Set<Keyz> phones = new HashSet<>();
        final Cursor phoneCursor = getPhoneNumbersCursor(contactContentId);

        if (phoneCursor != null) {
            Diagnostic.i("phones count", phoneCursor.getCount());
            if (phoneCursor.getCount() > 0) {
                while (phoneCursor.moveToNext()) {
                    final String value = phoneCursor
                            .getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replaceAll("[^0-9#*]", "");
                    if (!value.isEmpty()) {
                        final Keyz phone = new Keyz(
                                value,
                                KeyzType.Types.PHONE_NUMBER.getId()
                        );
                        phones.add(phone);
                    }
                }
            }
            phoneCursor.close();
        }
        Diagnostic.i("phones", phones);
        return phones;
    }

    /**
     * Создает и возвращает список адресов электронной почты для заданного контакта.
     *
     * @param contactContentId Идентификатор контакта в центральном репозитории.
     * @return Список адресов электронной почты.
     * @throws SecurityException Генерируется если доступ к чтению контактов запрещен.
     */
    private List<Keyz> getEmails (@NonNull final Long contactContentId) throws SecurityException {
        Diagnostic.i("contactContentId", contactContentId);
        final List<Keyz> emails = new ArrayList<>();
        final Cursor emailsCursor = getEmailsCursor(contactContentId);

        if (emailsCursor != null) {
            Diagnostic.i("emails count", emailsCursor.getCount());
            if (emailsCursor.getCount() > 0) {
                while (emailsCursor.moveToNext()) {
                    final Keyz email = new Keyz(
                            emailsCursor.getString(emailsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)),
                            KeyzType.Types.EMAIL.getId()
                    );
                    emails.add(email);
                }
            }
            emailsCursor.close();
        }
        Diagnostic.i("emails", emails);
        return emails;
    }

    /**
     * Возвращает курсор для телефонных номеров заданного контакта.
     *
     * @param contactContentId Идентификатор контакта в центральном репозитории.
     * @return Курсор для телефонных номеров.
     */
    private Cursor getPhoneNumbersCursor (@NonNull final Long contactContentId) {
        final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        final String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
        };
        final String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        final String[] selectionArgs = new String[]{contactContentId.toString()};
        return getCursor(
                uri,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    /**
     * Возвращает курсор для адресов электронной почты заданного контакта.
     *
     * @param contactContentId Идентификатор контакта в центральном репозитории.
     * @return Курсор для адресов электронной почты.
     */
    private Cursor getEmailsCursor (@NonNull final Long contactContentId) {
        final Uri uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        final String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.LABEL
        };
        final String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
        final String[] selectionArgs = new String[]{contactContentId.toString()};

        return getCursor(
                uri,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    private Cursor getRawContactsCursor (@NonNull final Long contactContentId) {
        final Uri uri = ContactsContract.RawContacts.CONTENT_URI;
        final String[] projection = new String[]{
                ContactsContract.RawContacts._ID
        };
        final String selection = ContactsContract.RawContacts.CONTACT_ID + " = ?";
        final String[] selectionArgs = new String[]{
                contactContentId.toString()
        };
        return getCursor(
                uri,
                projection,
                selection,
                selectionArgs,
                null
        );
    }

    /**
     * Посылает запрос поставщику контента и возвращает курсор.
     *
     * @param uri           Uri контента.
     * @param projection    Массив необходимых колонок.
     * @param selection     Фильтр.
     * @param selectionArgs Аргументы для фильтра.
     * @param sortOrder     Порядок сортировки.
     * @return Курсор.
     */
    private Cursor getCursor (
            @NonNull final Uri uri,
            @Nullable final String[] projection,
            @Nullable final String selection,
            @Nullable final String[] selectionArgs,
            @Nullable final String sortOrder
    ) {
        return this.mContentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    final void diagnosticContacts () {
        Diagnostic.i();

        Cursor cursor1 = mContentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.IN_VISIBLE_GROUP,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER},
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + ", " + ContactsContract.Contacts.LOOKUP_KEY + ", " + ContactsContract.Contacts._ID
        );

        Diagnostic.i("CONTACTS");
        if (cursor1 != null && cursor1.getCount() > 0) {
            Diagnostic.i("count", cursor1.getCount());
            while (cursor1.moveToNext()) {
                Diagnostic.i(ContactsContract.RawContacts.CONTACT_ID + " = " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID)) + ";\t" +
                        ContactsContract.Contacts.IN_VISIBLE_GROUP + " = " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.IN_VISIBLE_GROUP)) + ";\t" +
                        ContactsContract.Contacts.HAS_PHONE_NUMBER + " = " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) + ";\t" +
                        ContactsContract.Contacts.DISPLAY_NAME + " = " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + ";\t" +
                        ContactsContract.Contacts.PHOTO_URI + " = " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)) + ";\t" +
                        ContactsContract.Contacts.LOOKUP_KEY + " = " + cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)) + ";\t"
                );
            }
            cursor1.close();
        }


        Cursor cursor11 = mContentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.IN_VISIBLE_GROUP,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER},
                ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ?",
                new String[]{"1"},
                ContactsContract.Contacts.DISPLAY_NAME + ", " + ContactsContract.Contacts.LOOKUP_KEY + ", " + ContactsContract.Contacts._ID
        );

        Diagnostic.i("VISIBLE CONTACTS");
        if (cursor11 != null && cursor11.getCount() > 0) {
            Diagnostic.i("count", cursor11.getCount());
            while (cursor11.moveToNext()) {
                Diagnostic.i(ContactsContract.RawContacts.CONTACT_ID + " = " + cursor11.getString(cursor11.getColumnIndex(ContactsContract.Contacts._ID)) + ";\t" +
                        ContactsContract.Contacts.IN_VISIBLE_GROUP + " = " + cursor11.getString(cursor11.getColumnIndex(ContactsContract.Contacts.IN_VISIBLE_GROUP)) + ";\t" +
                        ContactsContract.Contacts.HAS_PHONE_NUMBER + " = " + cursor11.getString(cursor11.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) + ";\t" +
                        ContactsContract.Contacts.DISPLAY_NAME + " = " + cursor11.getString(cursor11.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + ";\t" +
                        ContactsContract.Contacts.PHOTO_URI + " = " + cursor11.getString(cursor11.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)) + ";\t" +
                        ContactsContract.Contacts.LOOKUP_KEY + " = " + cursor11.getString(cursor11.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)) + ";\t"
                );
            }
            cursor11.close();
        }

        Cursor cursor2 = mContentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID,
                        ContactsContract.RawContacts.ACCOUNT_TYPE,
                        ContactsContract.RawContacts.ACCOUNT_NAME
                },
                null,
                null,
                ContactsContract.RawContacts.CONTACT_ID
        );
        Diagnostic.i("RAW_CONTACTS");
        if (cursor2 != null && cursor2.getCount() > 0) {
            Diagnostic.i("count", cursor2.getCount());
            while (cursor2.moveToNext()) {
                Diagnostic.i(ContactsContract.Data.RAW_CONTACT_ID + " = " + cursor2.getString(cursor2.getColumnIndex(ContactsContract.RawContacts._ID)) + ";\t" +
                        ContactsContract.RawContacts.CONTACT_ID + " = " + cursor2.getString(cursor2.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)) + ";\t" +
                        ContactsContract.RawContacts.ACCOUNT_TYPE + " = " + cursor2.getString(cursor2.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)) + ";\t" +
                        ContactsContract.RawContacts.ACCOUNT_NAME + " = " + cursor2.getString(cursor2.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)) + ";\t"
                );
            }
            cursor2.close();
        }

        Cursor cursor3 = mContentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data._ID,
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.Data.RAW_CONTACT_ID,
                        ContactsContract.Data.DISPLAY_NAME,
                        ContactsContract.Data.LOOKUP_KEY,
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.Data.DATA1,
                        ContactsContract.Data.DATA2,
                        ContactsContract.Data.DATA3,
                        ContactsContract.Data.DATA4,
                        ContactsContract.Data.DATA5,
                        ContactsContract.Data.DATA6,
                        ContactsContract.Data.DATA7,
                        ContactsContract.Data.DATA8,
                        ContactsContract.Data.DATA9,
                        ContactsContract.Data.DATA10,
                        ContactsContract.Data.DATA11,
                        ContactsContract.Data.DATA12,
                        ContactsContract.Data.DATA13,
                        ContactsContract.Data.DATA14,
                        ContactsContract.Data.DATA15,
                        ContactsContract.Data.PHOTO_FILE_ID,
                        ContactsContract.Data.PHOTO_ID,
                        ContactsContract.Data.PHOTO_THUMBNAIL_URI,
                        ContactsContract.Data.PHOTO_URI},
                null,
                null,
                ContactsContract.Data.DISPLAY_NAME + ", " +
                        ContactsContract.Data.CONTACT_ID + ", " +
                        ContactsContract.Data.RAW_CONTACT_ID + ", " +
                        ContactsContract.Data.MIMETYPE
        );

        Diagnostic.i("DATA");
        if (cursor3 != null && cursor3.getCount() > 0) {
            Diagnostic.i("count", cursor3.getCount());
            while (cursor3.moveToNext()) {
                final byte[] data15 = cursor3.getBlob(cursor3.getColumnIndex(ContactsContract.Data.DATA15));
                Diagnostic.i("data_id = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data._ID)) + ";\t" +
                        ContactsContract.Data.CONTACT_ID + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.CONTACT_ID)) + ";\t" +
                        ContactsContract.Data.RAW_CONTACT_ID + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)) + ";\t" +
                        ContactsContract.Data.DISPLAY_NAME + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)) + ";\t" +
                        ContactsContract.Data.MIMETYPE + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.MIMETYPE)) + ";\t" +
                        ContactsContract.Data.DATA1 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA1)) + ";\t" +
                        ContactsContract.Data.DATA2 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA2)) + ";\t" +
                        ContactsContract.Data.DATA3 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA3)) + ";\t" +
                        ContactsContract.Data.DATA4 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA4)) + ";\t" +
                        ContactsContract.Data.DATA5 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA5)) + ";\t" +
                        ContactsContract.Data.DATA6 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA6)) + ";\t" +
                        ContactsContract.Data.DATA7 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA7)) + ";\t" +
                        ContactsContract.Data.DATA8 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA8)) + ";\t" +
                        ContactsContract.Data.DATA9 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA9)) + ";\t" +
                        ContactsContract.Data.DATA10 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA10)) + ";\t" +
                        ContactsContract.Data.DATA11 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA11)) + ";\t" +
                        ContactsContract.Data.DATA12 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA12)) + ";\t" +
                        ContactsContract.Data.DATA13 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA13)) + ";\t" +
                        ContactsContract.Data.DATA14 + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.DATA14)) + ";\t" +
                        ContactsContract.Data.DATA15 + " = " + (data15 == null ? "null" : Arrays.toString(data15)) + ";\t" +
                        ContactsContract.Data.PHOTO_FILE_ID + " = " + cursor3.getInt(cursor3.getColumnIndex(ContactsContract.Data.PHOTO_FILE_ID)) + ";\t" +
                        ContactsContract.Data.PHOTO_ID + " = " + cursor3.getInt(cursor3.getColumnIndex(ContactsContract.Data.PHOTO_ID)) + ";\t" +
                        ContactsContract.Data.PHOTO_THUMBNAIL_URI + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI)) + ";\t" +
                        ContactsContract.Data.PHOTO_URI + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.PHOTO_URI)) + ";\t" +
                        ContactsContract.Data.LOOKUP_KEY + " = " + cursor3.getString(cursor3.getColumnIndex(ContactsContract.Data.LOOKUP_KEY)) + ";\t"
                );
            }
            cursor3.close();
        }

        Cursor cursor4 = mContentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE,
                        ContactsContract.Groups.GROUP_VISIBLE,
                        ContactsContract.Groups.GROUP_IS_READ_ONLY,
                        ContactsContract.Groups.ACCOUNT_TYPE,
                        ContactsContract.Groups.ACCOUNT_NAME
                },
                null,
                null,
                ContactsContract.Groups._ID
        );
        Diagnostic.i("GROUPS");
        if (cursor4 != null && cursor4.getCount() > 0) {
            Diagnostic.i("count", cursor4.getCount());
            while (cursor4.moveToNext()) {
                Diagnostic.i("group_id = " + cursor4.getString(cursor4.getColumnIndex(ContactsContract.Groups._ID)) + ";\t" +
                        ContactsContract.Groups.TITLE + " = " + cursor4.getString(cursor4.getColumnIndex(ContactsContract.Groups.TITLE)) + ";\t" +
                        ContactsContract.Groups.GROUP_VISIBLE + " = " + cursor4.getString(cursor4.getColumnIndex(ContactsContract.Groups.GROUP_VISIBLE)) + ";\t" +
                        ContactsContract.Groups.GROUP_IS_READ_ONLY + " = " + cursor4.getString(cursor4.getColumnIndex(ContactsContract.Groups.GROUP_IS_READ_ONLY)) + ";\t" +
                        ContactsContract.Groups.ACCOUNT_TYPE + " = " + cursor4.getString(cursor4.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE)) + ";\t" +
                        ContactsContract.Groups.ACCOUNT_NAME + " = " + cursor4.getString(cursor4.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME)) + ";\t"
                );
            }
            cursor4.close();
        }
    }

}
