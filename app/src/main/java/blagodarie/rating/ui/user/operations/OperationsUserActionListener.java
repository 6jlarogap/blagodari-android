package blagodarie.rating.ui.user.operations;

import androidx.annotation.NonNull;

import blagodarie.rating.OperationType;

public interface OperationsUserActionListener {

    void onAddOperation (@NonNull final OperationType operationType);

}
