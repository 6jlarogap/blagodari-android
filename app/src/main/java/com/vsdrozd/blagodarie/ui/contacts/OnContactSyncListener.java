package com.vsdrozd.blagodarie.ui.contacts;

import androidx.annotation.NonNull;

public interface OnContactSyncListener {
    void onSync(@NonNull final Long contactId);
}
