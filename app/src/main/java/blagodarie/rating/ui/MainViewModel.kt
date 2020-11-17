package blagodarie.rating.ui

import android.accounts.Account
import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TAG: String = MainViewModel::class.java.name
    }

    val accountObserver = AccountObserver(application.applicationContext)
    val account = ObservableField<Account?>()

    init {
        accountObserver.observeForever {
            if (it != account.get()) {
                Log.d(TAG, "account changed from ${account.get()} to $it")
                account.set(it)
            }
        }
    }
}