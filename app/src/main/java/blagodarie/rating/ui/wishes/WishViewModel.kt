package blagodarie.rating.ui.wishes

import android.accounts.Account
import android.app.Application
import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import blagodarie.rating.R
import blagodarie.rating.model.IWish
import blagodarie.rating.model.entities.Wish
import blagodarie.rating.ui.createQrCodeBitmap

class WishViewModel(application: Application) : AndroidViewModel(application) {
    val isOwn = ObservableBoolean(false)
    val downloadInProgress = ObservableBoolean(false)
    val wish = ObservableField<IWish>(Wish.EMPTY_WISH)
    val qrCode = ObservableField<Bitmap>()
    val account = MutableLiveData<Account?>()
    val wishLiveData = MutableLiveData<IWish?>()

    init {
        account.observeForever {
            isOwn.set(isOwnWish(it, wish.get()))
        }
        wishLiveData.observeForever {
            isOwn.set(isOwnWish(account.value, it))
            wish.set(it)
            qrCode.set(if (it != null) createQrCodeBitmap(application.applicationContext.getString(R.string.url_wish, it.id)) else null)
        }
    }

    private fun isOwnWish(
            account: Account?,
            wish: IWish?
    ) = account != null && account.name == wish?.ownerId.toString()

    fun discardValues() {
        wish.set(Wish.EMPTY_WISH)
        qrCode.set(null)
        downloadInProgress.set(false)
        isOwn.set(false)
    }
}