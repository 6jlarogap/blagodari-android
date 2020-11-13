package blagodarie.rating.ui.profile

import android.accounts.Account
import android.app.Application
import android.graphics.Bitmap
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import blagodarie.rating.R
import blagodarie.rating.model.IProfile
import blagodarie.rating.model.entities.Profile
import blagodarie.rating.server.GetThanksUsersResponse.ThanksUser
import blagodarie.rating.ui.createQrCodeBitmap

class OwnProfileViewModel(application: Application) : AndroidViewModel(application) {
    val profileInfo = ObservableField<IProfile>(Profile.EMPTY_PROFILE)
    val qrCode = ObservableField<Bitmap>()
    val downloadInProgress = ObservableBoolean(false)
    var thanksUsers: LiveData<PagedList<ThanksUser>>? = null

    fun discardValues() {
        profileInfo.set(Profile.EMPTY_PROFILE)
        qrCode.set(null)
        downloadInProgress.set(false)
        thanksUsers = null
    }
}