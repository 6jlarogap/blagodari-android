package blagodarie.rating.ui

import android.accounts.Account
import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import blagodarie.rating.model.IWish

class WishViewModel : ViewModel() {
    val wish = ObservableField<IWish>()
    val qrCode = ObservableField<Bitmap>()
    val downloadInProgress = ObservableBoolean(false)
    val isOwn = ObservableBoolean(false)
    val account = MutableLiveData<Account>()
}