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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import blagodarie.rating.AppExecutors
import blagodarie.rating.R
import blagodarie.rating.databinding.OwnProfileFragmentBinding
import blagodarie.rating.model.IProfile
import blagodarie.rating.repository.AsyncServerRepository
import blagodarie.rating.server.GetThanksUsersResponse.ThanksUser
import blagodarie.rating.ui.AccountProvider
import blagodarie.rating.ui.AccountSource
import blagodarie.rating.ui.GridAutofitLayoutManager
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

    private lateinit var mViewModel: OwnProfileViewModel

    private lateinit var mBinding: OwnProfileFragmentBinding

    private lateinit var mThanksUsersAdapter: ThanksUsersAdapter

    private var mMiShare: MenuItem? = null

    private var mMiLogout: MenuItem? = null

    private var mIsFirstAccountRequest = true

    private val mAsyncRepository = AsyncServerRepository(AppExecutors.getInstance().networkIO(), AppExecutors.getInstance().mainThread())

    private val mAccountObserver = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            updateMenuItemsVisibility()
            refreshProfileData()
        }
    }

    val userActionListener = object : UserActionListener {

        override fun onOperationsClick() {
            Log.d(TAG, "onOperationsClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalOperationsFragment().setUserId(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onWishesClick() {
            Log.d(TAG, "onAbilitiesClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalWishesFragment(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onAbilitiesClick() {
            Log.d(TAG, "onAbilitiesClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalAbilitiesFragment(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onKeysClick() {
            Log.d(TAG, "onKeysClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalKeysFragment(UUID.fromString(mViewModel.account.get()?.name))
            NavHostFragment.findNavController(this@OwnProfileFragment).navigate(action)
        }

        override fun onSocialGraphClick() {
            Log.d(TAG, "onSocialGraphClick")
            val action: NavDirections = OwnProfileFragmentDirections.actionGlobalGraphFragment().setUserId(UUID.fromString(mViewModel.account.get()?.name))
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

        initViewModel()
        initThanksUserAdapter()
        setupBinding()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        AccountSource.getAccount(
                requireActivity(),
                mIsFirstAccountRequest
        ) {
            if (mViewModel.account.get() == null && it != null ||
                    mViewModel.account.get() != null && it == null ||
                    mViewModel.account.get() != null && it != null && mViewModel.account.get()!! != it) {
                mViewModel.account.set(it)
            }
        }
        if (mIsFirstAccountRequest) {
            mIsFirstAccountRequest = false
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
        mViewModel.account.removeOnPropertyChangedCallback(mAccountObserver)
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
        mThanksUsersAdapter.submitList(mViewModel.thanksUsers?.value)
    }

    private fun onThanksUserClick(userId: UUID) {
        Log.d(TAG, "onThanksUserClick")
        NavHostFragment.findNavController(this).navigate(Uri.parse(getString(R.string.url_profile, userId)))
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel")
        mViewModel = ViewModelProvider(requireActivity()).get(OwnProfileViewModel::class.java)
        mViewModel.discardValues()
        mViewModel.account.addOnPropertyChangedCallback(mAccountObserver)
    }

    private fun setupBinding() {
        Log.d(TAG, "setupBinding")
        mBinding.userActionListener = userActionListener
        mBinding.srlRefreshProfileInfo.setOnRefreshListener { this.refreshProfileData() }
        mBinding.rvThanksUsers.layoutManager = GridAutofitLayoutManager(requireContext(), (resources.getDimension(R.dimen.thanks_user_photo_width) + resources.getDimension(R.dimen.thanks_user_photo_margin) * 2).toInt())
        mBinding.rvThanksUsers.adapter = mThanksUsersAdapter
        mBinding.viewModel = mViewModel
        mBinding.btnEnter.setOnClickListener {
            AccountSource.getAccount(
                    requireActivity(),
                    true
            ) {
                if (mViewModel.account.get() == null && it != null ||
                        mViewModel.account.get() != null && it == null ||
                        mViewModel.account.get() != null && it != null && mViewModel.account.get()!! != it) {
                    mViewModel.account.set(it)
                }
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
        mMiShare?.isVisible = mViewModel.account.get() != null
        mMiLogout?.isVisible = mViewModel.account.get() != null
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

    private fun showLogoutConfirmDialog(){
        Log.d(TAG, "showLogoutConfirmDialog")
        AlertDialog.Builder(requireContext()).setMessage(R.string.qstn_realy_logout).setPositiveButton(R.string.btn_yes) { _: DialogInterface, _: Int ->
            logout()
        }.setNegativeButton(R.string.btn_no, null).show()
    }

    private fun logout() {
        Log.d(TAG, "logout")
        mViewModel.discardValues()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            AccountManager.get(requireContext()).removeAccount(
                    mViewModel.account.get(),
                    requireActivity(),
                    {
                        mViewModel.account.set(null)
                    },
                    null)
        } else {
            AccountManager.get(requireContext()).removeAccount(
                    mViewModel.account.get(),
                    {
                        mViewModel.account.set(null)
                    },
                    null)
        }
    }

    private fun refreshProfileData() {
        Log.d(TAG, "refreshProfileData")
        AccountSource.getAccount(
                requireActivity(),
                false
        ) { account: Account? ->
            if (mViewModel.account.get() == null && account != null ||
                    mViewModel.account.get() != null && account == null ||
                    mViewModel.account.get() != null && account != null && mViewModel.account.get()!! != account) {
                mViewModel.account.set(account)
            }
            if (account != null) {
                AccountProvider.getAuthToken(
                        requireActivity(),
                        account
                ) { authToken: String? -> this.downloadProfileData(UUID.fromString(account.name), authToken) }
            }
        }
    }

    private fun downloadProfileData(
            userId: UUID,
            authToken: String?
    ) {
        Log.d(TAG, "downloadProfileData")
        mViewModel.downloadInProgress.set(true)
        mAsyncRepository.setAuthToken(authToken)
        mAsyncRepository.getProfileInfo(
                userId,
                { profileInfo: IProfile? ->
                    mViewModel.downloadInProgress.set(false)
                    mViewModel.profileInfo.set(profileInfo)
                },
                { throwable: Throwable ->
                    mViewModel.downloadInProgress.set(false)
                    Toast.makeText(requireActivity(), throwable.message, Toast.LENGTH_LONG).show()
                })
        refreshThanksUsers(userId)
    }

    private fun refreshThanksUsers(
            userId: UUID
    ) {
        Log.d(TAG, "refreshThanksUsers")
        mViewModel.thanksUsers = mAsyncRepository.getLiveDataPagedListFromDataSource(ThanksUserDataSourceFactory(userId))
        mViewModel.thanksUsers?.observe(requireActivity(), androidx.lifecycle.Observer { pagedList: PagedList<ThanksUser?>? -> mThanksUsersAdapter.submitList(pagedList) })
    }

    private fun share() {
        Log.d(TAG, "onShareClick")
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_profile, mViewModel.account.get()?.name.toString()))
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Поделиться"))
    }
}