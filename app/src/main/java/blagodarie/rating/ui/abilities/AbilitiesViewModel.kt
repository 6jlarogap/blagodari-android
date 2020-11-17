package blagodarie.rating.ui.abilities

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IAbility

class AbilitiesViewModel : ViewModel() {
    var abilities: LiveData<PagedList<IAbility>>? = null
    val downloadInProgress = ObservableBoolean(false)
    val isEmpty = ObservableBoolean(false)
    val isOwn = ObservableBoolean(false)
}