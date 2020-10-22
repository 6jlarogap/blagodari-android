package blagodarie.rating.ui.user.anytext;

import androidx.annotation.NonNull;

import blagodarie.rating.model.entities.OperationType;

public interface AnyTextUserActionListener {

    void onShareAnyText ();

    void onAddOperation (@NonNull final OperationType operationType);

    void onOperations ();
}
