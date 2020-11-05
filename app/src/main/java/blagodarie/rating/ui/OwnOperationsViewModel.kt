package blagodarie.rating.ui

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IDisplayOperation

class OwnOperationsViewModel : ViewModel() {

    var operations: LiveData<PagedList<IDisplayOperation>>? = null

    val downloadInProgress = ObservableBoolean(false)

    val isEmpty = ObservableBoolean(false)
}