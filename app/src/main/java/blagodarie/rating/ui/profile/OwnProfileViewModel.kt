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
    val account: ObservableField<Account> = ObservableField()
    val profileInfo = ObservableField<IProfile>(Profile.EMPTY_PROFILE)
    val qrCode = ObservableField<Bitmap>()
    val downloadInProgress = ObservableBoolean(false)
    var thanksUsers: LiveData<PagedList<ThanksUser>>? = null

    init {
        account.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                qrCode.set(if (account.get() != null) createQrCodeBitmap(application.applicationContext.getString(R.string.url_profile, account.get()!!.name)) else null)
            }
        })
    }

    fun discardValues() {
        account.set(null)
        profileInfo.set(Profile.EMPTY_PROFILE)
        qrCode.set(null)
        downloadInProgress.set(false)
        thanksUsers = null
    }
}