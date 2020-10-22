package blagodarie.rating.ui.user.wishes;

import androidx.annotation.NonNull;

import blagodarie.rating.model.IWish;
import blagodarie.rating.model.entities.Wish;

public interface OnWishClickListener {

    void onClick (@NonNull final IWish wish);

}
