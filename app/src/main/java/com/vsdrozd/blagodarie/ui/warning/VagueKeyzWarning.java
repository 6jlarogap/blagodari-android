package com.vsdrozd.blagodarie.ui.warning;

import android.content.Intent;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vsdrozd.blagodarie.DataRepository;
import com.vsdrozd.blagodarie.db.addent.KeyzWithContacts;

/**
 * Предупреждение о ключе, привязанном к нескольким контактам.
 */
final class VagueKeyzWarning
        implements Warning {

    @NonNull
    private final KeyzWithContacts mKeyzWithContacts;

    VagueKeyzWarning (@NonNull final KeyzWithContacts keyzWithContacts) {
        this.mKeyzWithContacts = keyzWithContacts;
    }

    @Override
    public boolean resolve (
            @NonNull final AppCompatActivity activity,
            @NonNull final DataRepository dataRepository
    ) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
        return false;
    }

    @NonNull
    final KeyzWithContacts getKeyzWithContacts () {
        return this.mKeyzWithContacts;
    }
}
