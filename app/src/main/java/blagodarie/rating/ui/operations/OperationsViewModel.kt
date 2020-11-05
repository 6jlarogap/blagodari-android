package blagodarie.rating.ui.operations

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IDisplayOperation

class OperationsViewModel : ViewModel() {
    var operations: LiveData<PagedList<IDisplayOperation>>? = null
    val downloadInProgress = ObservableBoolean(false)
    val isEmpty = ObservableBoolean(false)
    val isOwn = ObservableBoolean(false)
}