package org.blagodari.ui.contacts;

import androidx.annotation.NonNull;

public interface OnContactSyncListener {
    void onSync(@NonNull final Long contactId);
}
