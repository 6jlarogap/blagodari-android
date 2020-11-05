package blagodarie.rating.ui

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IAbility
import blagodarie.rating.model.IWish

class WishesViewModel : ViewModel() {
    var wishes: LiveData<PagedList<IWish>>? = null
    val downloadInProgress = ObservableBoolean(false)
    val isEmpty = ObservableBoolean(false)
    val isOwn = ObservableBoolean(false)
}