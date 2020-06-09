package com.vsdrozd.blagodarie.ui.warning;

import androidx.annotation.NonNull;

import com.vsdrozd.blagodarie.db.scheme.Contact;

public interface OnContactClickListener {
    void onClick (@NonNull final Contact contact);
}
