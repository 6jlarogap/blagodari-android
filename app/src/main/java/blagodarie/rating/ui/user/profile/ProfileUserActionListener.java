package blagodarie.rating.ui.user.profile;

import androidx.annotation.NonNull;

import blagodarie.rating.model.entities.OperationType;

public interface ProfileUserActionListener {

    void onShareProfileClick ();

    void onTrustClick();

    void onMistrustClick();

    void onThanksClick();

    void onOperationsClick ();

    void onWishesClick ();

    void onAbilitiesClick ();

    void onKeysClick ();

    void onSocialGraphClick ();
}
