package com.vsdrozd.blagodarie.ui;

import androidx.annotation.NonNull;

import com.vsdrozd.blagodarie.db.scheme.Like;

/**
 * Интерфейс слушателя для получения события отмены Благораности.
 *
 * @author sergeGabrus
 */
public interface OnLikeCancelListener {

    /**
     * Вызывается при нажатии на кнопку отмены благодарности.
     *
     * @param like Отменяемая благодарность.
     */
    void onCancelLike (@NonNull final Like like);

}
