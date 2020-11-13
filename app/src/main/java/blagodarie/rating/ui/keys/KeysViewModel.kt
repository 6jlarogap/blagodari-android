package blagodarie.rating.ui.keys

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IKey

class KeysViewModel : ViewModel() {
    var keys: LiveData<PagedList<IKey>>? = null
    val downloadInProgress = ObservableBoolean(false)
    val isEmpty = ObservableBoolean(false)
    val isOwn = ObservableBoolean(false)
}