package blagodarie.rating.ui.profile

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.OwnProfileFragmentBinding
import blagodarie.rating.model.IProfile
import blagodarie.rating.model.entities.Profile
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.GetThanksUsersResponse.ThanksUser
import blagodarie.rating.ui.*
import blagodarie.rating.ui.profile.ThanksUsersDataSource.ThanksUserDataSourceFactory
import java.util.*

class OwnProfileFragment : Fragment() {

    interface UserActionListener {
        fun onOperationsClick()
        fun onWishesClick()
        fun onAbilitiesClick()
        fun onKeysClick()
        fun onSocialGraphClick()
    }

    companion object {
        private val TAG: String = OwnProfileFragment::class.java.name
    }

    private lateinit var mMainViewModel: MainViewModel

    private lateinit var mOwnProfileViewModel: OwnProfileViewModel

    private lateinit var mBinding: OwnProfileFragmentBinding

    private lateinit var mThanksUsersAdapter: ThanksUsersAdapter

    private var mMiShare: MenuItem? = null

    private var mMiLogout: MenuItem? = null

    private var mIsFirstShowing = true

    private val mAsyncRepository = AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread())

    private val mChangeAccountObserver = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (sender is ObservableField<*> && sender.get() is Account?) {
                Log.d(TAG, "mChangeAccountObserver.onPropertyChanged account=${sender.get()}")
                updateMenuItemsVisibility()
                refreshProfileData()
                mOwnProfileViewModel.qrCode.set(if (sender.get() != null) createQrCodeBitmap(getString(R.string.url_profile, (sender.get() as Account).name)) else null)
            }
        }
    }

    val userActionListener = object : UserActionListener {

        override fun onOperationsClick() {
            Log.d(TAG, "onOperationsClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalOperationsFragment().setUserId(UUID.fromString(mMainViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onWishesClick() {
            Log.d(TAG, "onAbilitiesClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalWishesFragment(UUID.fromString(mMainViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onAbilitiesClick() {
            Log.d(TAG, "onAbilitiesClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalAbilitiesFragment(UUID.fromString(mMainViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onKeysClick() {
            Log.d(TAG, "onKeysClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalKeysFragment(UUID.fromString(mMainViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onSocialGraphClick() {
            Log.d(TAG, "onSocialGraphClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalGraphFragment().setUserId(UUID.fromString(mMainViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        setHasOptionsMenu(true)
        initBinding(inflater, container)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

        initViewModels()
        initThanksUserAdapter()
        setupBinding()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        if (mMainViewModel.account.get() != null) {
            if (mOwnProfileViewModel.profileInfo.get() == Profile.EMPTY_PROFILE) {
                refreshProfileData()
            }
            mOwnProfileViewModel.qrCode.set(if (mMainViewModel.account.get() != null) createQrCodeBitmap(getString(R.string.url_profile, mMainViewModel.account.get()!!.name)) else null)
            mMainViewModel.account.addOnPropertyChangedCallback(mChangeAccountObserver)
        } else if (mIsFirstShowing) {
            mMainViewModel.account.addOnPropertyChangedCallback(mChangeAccountObserver)
            AccountSource.requireAccount(
                    requireActivity(),
            ) {
                mMainViewModel.accountObserver.value = it
            }
            mIsFirstShowing = false
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
        mMainViewModel.account.removeOnPropertyChangedCallback(mChangeAccountObserver)
    }

    private fun initBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ) {
        Log.d(TAG, "initBinding")
        mBinding = OwnProfileFragmentBinding.inflate(inflater, container, false)
    }

    private fun initThanksUserAdapter() {
        Log.d(TAG, "initThanksUserAdapter")
        mThanksUsersAdapter = ThanksUsersAdapter { userId: UUID -> onThanksUserClick(userId) }
        mThanksUsersAdapter.submitList(mOwnProfileViewModel.thanksUsers?.value)
    }

    private fun onThanksUserClick(userId: UUID) {
        Log.d(TAG, "onThanksUserClick")
        NavHostFragment.findNavController(this).navigate(Uri.parse(getString(R.string.url_profile, userId)))
    }

    private fun initViewModels() {
        Log.d(TAG, "initViewModel")
        mMainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        mOwnProfileViewModel = ViewModelProvider(requireActivity()).get(OwnProfileViewModel::class.java)
        mOwnProfileViewModel.discardValues()
        mOwnProfileViewModel.qrCode.set(if (mMainViewModel.account.get() != null) createQrCodeBitmap(getString(R.string.url_profile, mMainViewModel.account.get()!!.name)) else null)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.userActionListener = userActionListener
        mBinding.srlRefreshProfileInfo.setOnRefreshListener { this.refreshProfileData() }
        mBinding.rvThanksUsers.layoutManager = GridAutofitLayoutManager(requireContext(), (resources.getDimension(R.dimen.thanks_user_photo_width) + resources.getDimension(R.dimen.thanks_user_photo_margin) * 2).toInt())
        mBinding.rvThanksUsers.adapter = mThanksUsersAdapter
        mBinding.ownProfileViewModel = mOwnProfileViewModel
        mBinding.mainViewModel = mMainViewModel
        mBinding.btnEnter.setOnClickListener {
            AccountSource.requireAccount(
                    requireActivity(),
            ) {
                mMainViewModel.accountObserver.value = it
            }
        }
    }

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater
    ) {
        Log.d(TAG, "onCreateOptionsMenu")
        super.onCreateOptionsMenu(menu, inflater)
        requireActivity().menuInflater.inflate(R.menu.own_profile_fragment, menu)
        mMiShare = menu.findItem(R.id.miShare)
        mMiLogout = menu.findItem(R.id.miLogout)
        updateMenuItemsVisibility()
    }

    private fun updateMenuItemsVisibility() {
        Log.d(TAG, "updateMenuItemsVisibility")
        mMiShare?.isVisible = mMainViewModel.account.get() != null
        mMiLogout?.isVisible = mMainViewModel.account.get() != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        return when (item.itemId) {
            R.id.miShare -> {
                share()
                true
            }
            R.id.miLogout -> {
                showLogoutConfirmDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showLogoutConfirmDialog() {
        Log.d(TAG, "showLogoutConfirmDialog")
        AlertDialog.Builder(requireContext()).setMessage(R.string.qstn_realy_logout).setPositiveButton(R.string.btn_yes) { _: DialogInterface, _: Int ->
            logout()
        }.setNegativeButton(R.string.btn_no, null).show()
    }

    private fun logout() {
        Log.d(TAG, "logout")
        mOwnProfileViewModel.discardValues()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            AccountManager.get(requireContext()).removeAccount(
                    mMainViewModel.accountObserver.value,
                    requireActivity(),
                    {
                        mMainViewModel.accountObserver.value = null
                    },
                    null)
        } else {
            AccountManager.get(requireContext()).removeAccount(
                    mMainViewModel.accountObserver.value,
                    {
                        mMainViewModel.accountObserver.value = null
                    },
                    null)
        }
    }

    private fun refreshProfileData() {
        Log.d(TAG, "refreshProfileData")
        val account = mMainViewModel.accountObserver.value
        if (account != null) {
            AccountProvider.getAuthToken(
                    requireActivity(),
                    account
            ) { authToken: String? -> this.downloadProfileData(UUID.fromString(account.name), authToken) }
        }
    }

    private fun downloadProfileData(
            userId: UUID,
            authToken: String?
    ) {
        Log.d(TAG, "downloadProfileData")
        mOwnProfileViewModel.downloadInProgress.set(true)
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.getProfileInfo(
                userId,
                { profileInfo: IProfile? ->
                    mOwnProfileViewModel.downloadInProgress.set(false)
                    mOwnProfileViewModel.profileInfo.set(profileInfo)
                },
                { throwable: Throwable ->
                    mOwnProfileViewModel.downloadInProgress.set(false)
                    Toast.makeText(requireActivity(), throwable.message, Toast.LENGTH_LONG).show()
                })
        refreshThanksUsers(userId)
    }

    private fun refreshThanksUsers(
            userId: UUID
    ) {
        Log.d(TAG, "refreshThanksUsers")
        mOwnProfileViewModel.thanksUsers = mAsyncRepository.getLiveDataPagedListFromDataSource(ThanksUserDataSourceFactory(userId))
        mOwnProfileViewModel.thanksUsers?.observe(requireActivity(), androidx.lifecycle.Observer { pagedList: PagedList<ThanksUser?>? -> mThanksUsersAdapter.submitList(pagedList) })
    }

    private fun share() {
        Log.d(TAG, "onShareClick")
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_profile, mMainViewModel.account.get()?.name.toString()))
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Поделиться"))
    }
}