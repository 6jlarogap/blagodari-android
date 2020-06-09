package com.vsdrozd.blagodarie.db.addent;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.vsdrozd.blagodarie.db.scheme.Contact;
import com.vsdrozd.blagodarie.db.scheme.ContactKeyz;
import com.vsdrozd.blagodarie.db.scheme.Keyz;

import java.util.List;

public class KeyzWithContacts {
    @Embedded
    private final Keyz Keyz;

    @Relation (
            parentColumn = "id",
            entity = Contact.class,
            entityColumn = "id",
            associateBy = @Junction (
                    value = ContactKeyz.class,
                    parentColumn = "keyz_id",
                    entityColumn = "contact_id"
            )
    )
    private final List<Contact> ContactList;

    public KeyzWithContacts (
            @NonNull final Keyz Keyz,
            @NonNull final List<Contact> ContactList
    ) {
        this.Keyz = Keyz;
        this.ContactList = ContactList;
    }

    public final Keyz getKeyz () {
        return this.Keyz;
    }

    public final List<Contact> getContactList () {
        return this.ContactList;
    }
}
