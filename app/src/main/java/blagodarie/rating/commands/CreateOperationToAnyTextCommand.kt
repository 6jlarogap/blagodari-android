package blagodarie.rating.commands

import android.app.Activity
import android.util.Log
import blagodarie.rating.model.entities.OperationToAnyText
import blagodarie.rating.model.entities.OperationType
import blagodarie.rating.repository.AsyncRepository
import java.util.*

class CreateOperationToAnyTextCommand(
        private val activity: Activity,
        private val userIdFrom: UUID,
        private val textIdTo: UUID,
        private val operationType: OperationType,
        private val anyText: String,
        private val repository: AsyncRepository,
        private val onCompleteListener: AsyncRepository.OnCompleteListener,
        private val onErrorListener: AsyncRepository.OnErrorListener
) : CreateOperationCommand() {

    companion object {
        private val TAG: String = CreateOperationToAnyTextCommand::class.java.name
    }

    override fun execute() {
        Log.d(TAG, "execute")
        val addCommentListener = { comment: String ->
            val operationToAnyText = OperationToAnyText(userIdFrom, textIdTo, operationType, comment, Date())
            repository.insertOperationToAnyText(operationToAnyText, anyText, onCompleteListener, onErrorListener)
        }
        if (operationType == OperationType.NULLIFY_TRUST) {
            addCommentListener.invoke("")
        } else {
            showOperationCommentDialog(activity, addCommentListener)
        }
    }
}