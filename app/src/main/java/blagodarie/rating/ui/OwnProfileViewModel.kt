package blagodarie.rating.ui

import android.accounts.Account
import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import blagodarie.rating.model.IProfile
import blagodarie.rating.model.entities.Profile
import blagodarie.rating.server.GetThanksUsersResponse.ThanksUser

class OwnProfileViewModel : ViewModel() {

    val account: ObservableField<Account> = ObservableField()

    val profileInfo = ObservableField<IProfile>(Profile.EMPTY_PROFILE)

    val qrCode = ObservableField<Bitmap>()

    val downloadInProgress = ObservableBoolean(false)

    var thanksUsers: LiveData<PagedList<ThanksUser>>? = null

}