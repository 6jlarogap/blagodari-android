package com.vsdrozd.blagodarie.ui.contacts;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * Интерфейс слушателя для получения события нажатия на контакт
 *
 * @author sergeGabrus
 */
public interface ContactItemClickListener {

    /**
     * Вызывается при нажатии на контакт.
     *
     * @param view      View, на которое было произведено нажатие.
     * @param contactId Идентификатор контакта.
     */
    void onContactItemClick (
            @NonNull final View view,
            @NonNull final Long contactId
    );
}
