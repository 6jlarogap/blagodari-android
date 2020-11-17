package blagodarie.rating.commands

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import blagodarie.rating.R
import blagodarie.rating.databinding.EnterOperationCommentDialogBinding

abstract class CreateOperationCommand : Command {

    fun interface OnAddCommentListener {
        fun onCommentAdded(comment: String)
    }

    companion object {
        private val TAG: String = CreateOperationCommand::class.java.name
    }

    protected open fun showOperationCommentDialog(
            activity: Activity,
            onAddCommentListener: OnAddCommentListener
    ) {
        Log.d(TAG, "requireOperationComment")
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val binding: EnterOperationCommentDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.enter_operation_comment_dialog, null, false)

        AlertDialog.Builder(activity).setCancelable(false).setTitle(R.string.txt_comment).setView(binding.root).setNegativeButton(android.R.string.cancel)
        { _: DialogInterface?, _: Int ->
            imm.hideSoftInputFromWindow(binding.etOperationComment.windowToken, 0)
        }.setPositiveButton(android.R.string.ok)
        { _: DialogInterface?, _: Int ->
            imm.hideSoftInputFromWindow(binding.etOperationComment.windowToken, 0)
            val operationComment: String = binding.etOperationComment.text.toString().trim()
            onAddCommentListener.onCommentAdded(operationComment)
        }.create().show()

        binding.etOperationComment.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}