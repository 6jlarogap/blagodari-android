package org.blagodari.db.addent;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import org.blagodari.db.scheme.Contact;
import org.blagodari.db.scheme.ContactKeyz;
import org.blagodari.db.scheme.Keyz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public final class ContactWithKeyz {

    @NonNull
    @Embedded
    private final Contact Contact;

    @NonNull
    @Relation (
            parentColumn = "id",
            entity = Keyz.class,
            entityColumn = "id",
            associateBy = @Junction (
                    value = ContactKeyz.class,
                    parentColumn = "contact_id",
                    entityColumn = "keyz_id"
            )
    )
    private Set<Keyz> KeyzSet;

    public ContactWithKeyz (
            @NonNull final Contact Contact,
            @NonNull final Set<Keyz> KeyzSet
    ) {
        this.Contact = Contact;
        this.KeyzSet = KeyzSet;
    }

    @NonNull
    public final Contact getContact () {
        return this.Contact;
    }

    @NonNull
    public final Set<Keyz> getKeyzSet () {
        return this.KeyzSet;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ContactWithKeyz that = (ContactWithKeyz) o;
        return this.Contact.equals(that.Contact) &&
                this.KeyzSet.equals(that.KeyzSet);
    }

    @Override
    public int hashCode () {
        int result = 13;
        result = (47 * result) + this.Contact.hashCode();
        for (Keyz keyz : KeyzSet) {
            result = (47 * result) + keyz.hashCode();
        }
        return result;
    }

    @Override
    public String toString () {
        return "ContactWithKeyz{" +
                "Contact=" + Contact +
                ", KeyzSet=" + KeyzSet +
                '}';
    }

    public static Collection<Contact> extractContactList (@NonNull final Collection<ContactWithKeyz> contactWithKeyzCollection) {
        final Collection<Contact> contactCollection = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzCollection) {
            contactCollection.add(contactWithKeyz.getContact());
        }
        return contactCollection;
    }

    public static Collection<Keyz> extractKeyzList (@NonNull final Collection<ContactWithKeyz> contactWithKeyzCollection) {
        final Collection<Keyz> keyzCollection = new ArrayList<>();
        for (ContactWithKeyz contactWithKeyz : contactWithKeyzCollection) {
            keyzCollection.addAll(contactWithKeyz.getKeyzSet());
        }
        return keyzCollection;
    }

    public static Collection<Long> extractContactIds (@NonNull final Collection<Contact> contactCollection) {
        final Collection<Long> cotnactsIds = new ArrayList<>();
        for (Contact contact : contactCollection) {
            cotnactsIds.add(contact.getId());
        }
        return cotnactsIds;
    }

    public static Collection<Long> extractKeyzIds (@NonNull final Collection<Keyz> keyzCollection) {
        final Collection<Long> keyzIds = new ArrayList<>();
        for (Keyz keyz : keyzCollection) {
            keyzIds.add(keyz.getId());
        }
        return keyzIds;
    }
}
