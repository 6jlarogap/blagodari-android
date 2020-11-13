package blagodarie.rating.commands

import android.app.Activity
import android.util.Log
import blagodarie.rating.model.entities.OperationToUser
import blagodarie.rating.model.entities.OperationType
import blagodarie.rating.repository.AsyncRepository
import java.util.*

class CreateOperationToUserCommand(
        private val activity: Activity,
        private val userIdFrom: UUID,
        private val userIdTo: UUID,
        private val operationType: OperationType,
        private val repository: AsyncRepository,
        private val onCompleteListener: AsyncRepository.OnCompleteListener,
        private val onErrorListener: AsyncRepository.OnErrorListener
) : CreateOperationCommand() {

    companion object {
        private val TAG: String = CreateOperationToUserCommand::class.java.name
    }

    override fun execute() {
        Log.d(TAG, "execute")
        val addCommentListener = { comment: String ->
            val operationToUser = OperationToUser(userIdFrom, userIdTo, operationType, comment, Date())
            repository.insertOperationToUser(operationToUser, onCompleteListener, onErrorListener)
        }
        if (operationType == OperationType.NULLIFY_TRUST) {
            addCommentListener.invoke("")
        } else {
            showOperationCommentDialog(activity, addCommentListener)
        }
    }
}