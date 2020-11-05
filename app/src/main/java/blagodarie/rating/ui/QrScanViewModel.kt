package blagodarie.rating.ui

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel

class QrScanViewModel : ViewModel() {
    val isShowExplanation = ObservableBoolean(false)
}