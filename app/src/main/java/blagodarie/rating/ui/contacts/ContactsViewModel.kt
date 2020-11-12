package blagodarie.rating.ui.contacts

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IKeyPair
import blagodarie.rating.model.IProfile

class ContactsViewModel : ViewModel() {
    val textFilter = MutableLiveData<String>()
    val keysFilter = MutableLiveData<List<IKeyPair>>()
    lateinit var people: LiveData<PagedList<IProfile>>
    val downloadInProgress = ObservableBoolean(false)
    val isShowExplanation = ObservableBoolean(false)
    val isEmpty = ObservableBoolean(false)

    init {

    }
}