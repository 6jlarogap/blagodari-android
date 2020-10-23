package blagodarie.rating.operations;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import blagodarie.rating.R;
import blagodarie.rating.databinding.EnterOperationCommentDialogBinding;

abstract class OperationManager {

    private static final String TAG = OperationManager.class.getSimpleName();

    interface OnAddCommentListener {
        void onCommentAdded (@NonNull final String comment);
    }

    protected void showOperationCommentDialog (
            @NonNull final Activity activity,
            @NonNull final OnAddCommentListener onAddCommentListener
    ) {
        Log.d(TAG, "requireOperationComment");
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        final EnterOperationCommentDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.enter_operation_comment_dialog, null, false);
        new AlertDialog.
                Builder(activity).
                setCancelable(false).
                setTitle(R.string.txt_comment).
                setView(binding.getRoot()).
                setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0)).
                setPositiveButton(android.R.string.ok,
                        (dialogInterface, i) -> {
                            imm.hideSoftInputFromWindow(binding.etOperationComment.getWindowToken(), 0);
                            final String operationComment = binding.etOperationComment.getText().toString();
                            onAddCommentListener.onCommentAdded(operationComment);
                        }).
                create().
                show();
        binding.etOperationComment.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}
