package blagodarie.rating.ui.contacts

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import blagodarie.rating.model.IKeyPair
import blagodarie.rating.model.entities.KeyPair
import blagodarie.rating.model.entities.KeyType
import java.util.concurrent.Executor

class ContactsRepository : IContactsRepository {

    override fun getKeys(
            executor: Executor,
            mainThreadExecutor: Executor,
            contentResolver: ContentResolver,
            onLoadListener: IContactsRepository.OnLoadListener,
            onErrorListener: IContactsRepository.OnErrorListener
    ) {
        executor.execute {
            try {
                val keys: List<IKeyPair> = readContactKeys(contentResolver)
                mainThreadExecutor.execute { onLoadListener.onLoad(keys) }
            } catch (throwable: Throwable) {
                mainThreadExecutor.execute { onErrorListener.onError(throwable) }
            }
        }
    }

    private fun readContactKeys(
            contentResolver: ContentResolver
    ): List<IKeyPair> {
        val keys: MutableList<IKeyPair> = ArrayList()
        keys.addAll(getPhones(contentResolver))
        keys.addAll(getEmails(contentResolver))
        return keys
    }

    private fun getPhones(
            contentResolver: ContentResolver
    ): List<IKeyPair> {
        val phones: MutableList<IKeyPair> = ArrayList()
        val phoneCursor = getPhoneNumbersCursor(contentResolver)
        if (phoneCursor != null) {
            if (phoneCursor.count > 0) {
                while (phoneCursor.moveToNext()) {
                    val value = phoneCursor
                            .getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replace("[^0-9#*+]".toRegex(), "")
                    if (!value.isEmpty()) {
                        val phone = KeyPair(
                                value,
                                KeyType.PHONE
                        )
                        phones.add(phone)
                    }
                }
            }
            phoneCursor.close()
        }
        return phones
    }

    private fun getEmails(
            contentResolver: ContentResolver
    ): List<IKeyPair> {
        val emails: MutableList<IKeyPair> = ArrayList()
        val emailsCursor = getEmailsCursor(contentResolver)
        if (emailsCursor != null) {
            if (emailsCursor.count > 0) {
                while (emailsCursor.moveToNext()) {
                    val email = KeyPair(
                            emailsCursor.getString(emailsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)),
                            KeyType.EMAIL
                    )
                    emails.add(email)
                }
            }
            emailsCursor.close()
        }
        return emails
    }

    private fun getPhoneNumbersCursor(
            contentResolver: ContentResolver
    ): Cursor? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
        )
        return getCursor(
                contentResolver,
                uri,
                projection
        )
    }

    private fun getEmailsCursor(
            contentResolver: ContentResolver
    ): Cursor? {
        val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
        val projection = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.LABEL
        )
        return getCursor(
                contentResolver,
                uri,
                projection
        )
    }

    private fun getCursor(
            contentResolver: ContentResolver,
            uri: Uri,
            projection: Array<String>?
    ): Cursor? {
        return contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
        )
    }

}