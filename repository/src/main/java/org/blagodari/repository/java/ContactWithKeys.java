package org.blagodari.repository.java;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class ContactWithKeys {

    @NonNull
    @Embedded
    private final Contact Contact;

    @NonNull
    @Relation (
            parentColumn = "id",
            entity = Key.class,
            entityColumn = "id",
            associateBy = @Junction (
                    value = ContactKey.class,
                    parentColumn = "contact_id",
                    entityColumn = "key_id"
            )
    )
    private final Set<Key> Keys;

    ContactWithKeys (
            @NonNull final Contact Contact,
            @NonNull final Set<Key> Keys
    ) {
        this.Contact = Contact;
        this.Keys = Keys;
    }

    @NonNull
    final Contact getContact () {
        return this.Contact;
    }

    @NonNull
    final Set<Key> getKeySet () {
        return this.Keys;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ContactWithKeys that = (ContactWithKeys) o;
        return this.Contact.equals(that.Contact) &&
                this.Keys.equals(that.Keys);
    }

    @Override
    public int hashCode () {
        int result = 13;
        result = (47 * result) + this.Contact.hashCode();
        for (Key key : Keys) {
            result = (47 * result) + key.hashCode();
        }
        return result;
    }

    public static Collection<Contact> extractContactList (@NonNull final Collection<ContactWithKeys> contactWithKeysCollection) {
        final Collection<Contact> contacts = new ArrayList<>();
        for (ContactWithKeys contactWithKeys : contactWithKeysCollection) {
            contacts.add(contactWithKeys.getContact());
        }
        return contacts;
    }

    public static Collection<Key> extractKeyList (@NonNull final Collection<ContactWithKeys> contactWithKeysCollection) {
        final Collection<Key> keyList = new ArrayList<>();
        for (ContactWithKeys contactWithKeys : contactWithKeysCollection) {
            keyList.addAll(contactWithKeys.getKeySet());
        }
        return keyList;
    }

    public static Collection<Long> extractContactIds (@NonNull final Collection<Contact> contactCollection) {
        final List<Long> cotnactsIds = new ArrayList<>();
        for (Contact contact : contactCollection) {
            cotnactsIds.add(contact.getId());
        }
        return cotnactsIds;
    }

    public static Collection<Long> extractKeyIds (@NonNull final Collection<Key> keyCollection) {
        final Collection<Long> keyIds = new ArrayList<>();
        for (Key key : keyCollection) {
            keyIds.add(key.getId());
        }
        return keyIds;
    }
}
