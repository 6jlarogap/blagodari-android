package org.blagodari.ui.warning;

import androidx.annotation.NonNull;

import org.blagodari.db.scheme.Contact;

public interface OnContactClickListener {
    void onClick (@NonNull final Contact contact);
}
