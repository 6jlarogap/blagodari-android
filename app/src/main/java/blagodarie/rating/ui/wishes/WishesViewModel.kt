package blagodarie.rating.ui.wishes

import android.accounts.Account
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IWish
import java.util.*

class WishesViewModel : ViewModel() {
    val isOwn = ObservableBoolean(false)
    val downloadInProgress = ObservableBoolean(false)
    lateinit var wishes: LiveData<PagedList<IWish?>>
    val isEmpty = ObservableBoolean(false)
    var userId: UUID? = null
    val account = MutableLiveData<Account?>()

    init {
        account.observeForever {
            isOwn.set(it != null && it.name == userId.toString())
        }
    }

    fun discardValues() {
        account.value = null
        isOwn.set(false)
        downloadInProgress.set(false)
        isEmpty.set(false)
        userId = null
    }
}