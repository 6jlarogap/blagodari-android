package blagodarie.rating.ui

import android.accounts.Account
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent

class AccountObserver(
        private val context: Context
) :
        MutableLiveData<Account?>(),
        LifecycleObserver {

    companion object {
        private val TAG: String = AccountObserver::class.java.name
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
    fun updateAccount() {
        Log.d(TAG, "updateAccount")
        AccountSource.getAccount(
                context
        ) {
            value = it
        }
    }
}