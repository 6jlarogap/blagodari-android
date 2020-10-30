package blagodarie.rating.ui.contacts

import android.content.ContentResolver
import blagodarie.rating.model.IKeyPair
import java.util.concurrent.Executor

interface IContactsRepository {

    interface OnLoadListener {
        fun onLoad(value: List<IKeyPair>)
    }

    interface OnErrorListener {
        fun onError(throwable: Throwable)
    }

    fun getKeys(
            executor: Executor,
            mainThreadExecutor: Executor,
            contentResolver: ContentResolver,
            onLoadListener: OnLoadListener,
            onErrorListener: OnErrorListener
    )
}