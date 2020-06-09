package com.vsdrozd.blagodarie.ui.newcontacts;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NewContact {

    @NonNull
    private final String Name;

    @NonNull
    private final Set<NewPhone> Phones = new HashSet<>();

    @NonNull
    private final Set<NewEmail> Emails = new HashSet<>();

    @NonNull
    private final Set<NewKey> Keys = new HashSet<>();

    public NewContact (
            @NonNull final String name,
            @NonNull final Collection<NewPhone> phones,
            @NonNull final Collection<NewEmail> emails) {
        this.Name = name;
        this.Phones.addAll(phones);
        this.Emails.addAll(emails);
        this.Keys.addAll(this.Phones);
        this.Keys.addAll(this.Emails);
    }
}
