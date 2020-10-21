package blagodarie.rating.ui.user.profile;

import androidx.annotation.NonNull;

import blagodarie.rating.OperationType;

public interface ProfileUserActionListener {

    void onShareProfile ();

    void onAddOperation (@NonNull final OperationType operationType);

    void onOperations ();

    void onWishes ();

    void onAbilities ();

    void onKeys ();

    void onSocialGraph ();
}
