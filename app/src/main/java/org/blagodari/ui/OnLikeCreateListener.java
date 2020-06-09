package org.blagodari.ui;

import androidx.annotation.NonNull;

/**
 * Интерфейс слушателя для получения события создания Благораности.
 *
 * @author sergeGabrus
 */
public interface OnLikeCreateListener {

    /**
     * Вызывается при нажатии на кнопку благодарности.
     *
     * @param contactId Идентификатор благодаримого контакта.
     */
    void onCreateLike (@NonNull final Long contactId);

}
