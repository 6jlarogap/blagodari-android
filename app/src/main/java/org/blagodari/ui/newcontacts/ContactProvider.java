package org.blagodari.ui.newcontacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ex.diagnosticlib.Diagnostic;
import org.blagodari.db.addent.ContactWithKeyz;
import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.Keyz;
import org.blagodari.db.scheme.KeyzType;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactProvider
        implements Publisher<ContactWithKeyz> {

    interface ProgressListener{
        void onStart();

        void onGetData(int index, int sizwe);
        void onFinish();
    }

    /**
     * Поставщик контента.
     */
    @NonNull
    private final ContentResolver mContentResolver;

    private ProgressListener progressListener;

    ContactProvider (@NonNull final ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    public void setProgressListener (ProgressListener progressListener) {
        this.progressListener = progressListener;
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

            Diagnostic.i("contentContactId", contactId);
            Diagnostic.i("photoUri", photoUri);

            //создать контакт
            final Contact contact = new Contact(title);
            contact.setId(contactId);
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

    @Override
    public void subscribe (@NonNull final Subscriber<? super ContactWithKeyz> s) {
        Diagnostic.i();
        if (progressListener != null) {
            progressListener.onStart();
        }
        try {
            //Получить курсор контактов
            final Cursor contactsCursor = getContactsCursor();
            //Если курсор не пустой
            if (contactsCursor != null) {
                Diagnostic.i("contacts count", contactsCursor.getCount());
                int index = 1;
                //Пока в курсоре есть записи
                while (contactsCursor.moveToNext()) {
                    if (progressListener != null) {
                        progressListener.onGetData(index++, contactsCursor.getCount());
                    }
                    final long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    //Попытаться создать контакт с ключами
                    final ContactWithKeyz contactWithKeyz = attemptCreateContactWithKeyz(contactId, contactsCursor);
                    //если удалось создать
                    if (contactWithKeyz != null) {
                        s.onNext(contactWithKeyz);
                    }
                }
                //Закрыть курсор
                contactsCursor.close();
            }
            s.onComplete();
        }catch (Throwable throwable){
            s.onError(throwable);
        }
        if (progressListener != null) {
            progressListener.onFinish();
        }
    }
}
