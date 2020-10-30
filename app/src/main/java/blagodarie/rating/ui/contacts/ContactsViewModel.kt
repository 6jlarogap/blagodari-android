package blagodarie.rating.ui.contacts

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IKeyPair
import blagodarie.rating.model.IProfile

class ContactsViewModel : ViewModel() {

    val downloadInProgress: ObservableBoolean = ObservableBoolean(false)
        get() {
            return field
        }

    var keys: List<IKeyPair> = emptyList()
    var people: LiveData<PagedList<IProfile>>? = null
}